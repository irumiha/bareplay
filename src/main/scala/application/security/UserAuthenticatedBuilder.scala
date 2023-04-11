package application.security

import org.json4s.*
import org.json4s.DefaultJsonFormats.*
import pdi.jwt.{JwtAlgorithm, JwtJson4s, JwtOptions}
import play.api.Configuration
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.mvc.*
import play.api.mvc.Security.AuthenticatedRequest

import java.time.{Clock, Instant}
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

/** Extracts authentication information from the incoming request.
  *
  * @param config
  *   Application configuration
  * @param realmInfoService
  *   Access to realm info endpoint. Really needed just for the JWT issuer public key
  */
class AuthenticationExtractor(
    config: Configuration,
    realmInfoService: RealmInfoService,
    clock: Clock
) extends play.api.Logging:

  private val cookieName    = config.getOptional[String]("security.session_cookie_name")
  private val allowedIssuer = config.get[String]("security.oauth2_oidc.token_issuer")

  private def extractFromCookie(request: RequestHeader) =
    cookieName
      .flatMap(request.cookies.get)
      .map(_.value)
      .flatMap(decodeJwt)

  private def extractFromAuthorization(request: RequestHeader) =
    request.headers
      .get(HeaderNames.AUTHORIZATION)
      .flatMap(tokenValueFromHeader)
      .flatMap(decodeJwt)

  private def tokenValueFromHeader(authorization: String): Option[String] =
    authorization.split("Bearer ").toList.drop(1).headOption

  private[security] def decodeJwt(jwtValue: String): Option[Authentication] =
    val authentication =
      JwtJson4s
        .decodeJson(
          jwtValue,
          realmInfoService.realmInfo.publicKey,
          Seq(JwtAlgorithm.RS256),
          JwtOptions.DEFAULT.copy(expiration = false)
        )
        .filter(j => (j \ "iss").as[String] == allowedIssuer)
        .map(claimsToAuthentication)

    authentication match
      case Success(auth) => Some(auth)
      case Failure(ex) =>
        logger.error(ex.getMessage)
        None

  private def claimsToAuthentication(claimsJson: JObject) =
    Authentication(
      identity = (claimsJson \ "sub").as[String],
      username = (claimsJson \ "preferred_username").as[String],
      firstName = (claimsJson \ "given_name").as[Option[String]].filterNot(_.isEmpty),
      familyName = (claimsJson \ "family_name").as[Option[String]].filterNot(_.isEmpty),
      roles = (claimsJson \ "realm_access" \ "roles").as[Set[String]],
      expired = !Instant.ofEpochSecond((claimsJson \ "exp").as[Long]).isAfter(Instant.now(clock)),
      attributes = Map.empty
    )

  /** Extract authentication information from request headers. If cookie name was given use it to
    * extract JWT, if cookie name was not provided or extraction failed, try the Authorization
    * header.
    *
    * @param request
    *   Incoming request
    * @return
    *   Authentication data
    */
  def extract(request: RequestHeader): Option[Authentication] =
    extractFromCookie(request) orElse extractFromAuthorization(request)

case class Unauthenticated(configuration: Configuration) extends Status:
  private val nextUrlCookieName = configuration.get[String]("security.login_redirect_cookie_name")
  private val authUri           = configuration.get[String]("security.oauth2_oidc.auth_url")
  private val clientId          = configuration.get[String]("security.oauth2_oidc.client_id")
  private val redirectUri       = configuration.get[String]("security.oauth2_oidc.redirect_uri")

  private val redirectParams =
    Map(
      "scope"         -> Seq("openid"),
      "response_type" -> Seq("code"),
      "client_id"     -> Seq(clientId),
      "redirect_uri"  -> Seq(redirectUri),
      "state"         -> Seq(UUID.randomUUID().toString)
    )

  /** If the request was for a HTML page redirect to login page on Oauth2 provider, otherwise return
    * Unauthorized (http status 401)
    * @param request
    *   Incoming request
    * @return
    *   Redirect response or http status 401
    */
  def respond(request: RequestHeader): Result =
    if request.accepts(MimeTypes.HTML) || request.accepts(MimeTypes.XHTML) then
      Results
        .Redirect(authUri, redirectParams, SEE_OTHER)
        .withCookies(
          Cookie(nextUrlCookieName, request.uri)
        )
    else Results.Unauthorized("")

class UserAuthenticatedBuilder(
    cc: ControllerComponents,
    configuration: Configuration,
    realmInfoService: RealmInfoService,
    sessionCache: AuthenticationCache,
    keycloakTokens: KeycloakTokens,
    clock: Clock
)(override implicit val executionContext: ExecutionContext)
    extends ActionBuilder[({ type R[A] = AuthenticatedRequest[A, Authentication] })#R, AnyContent]
    with play.api.Logging:
  override def parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser

  private val cookieName = configuration.get[String]("security.session_cookie_name")
  private val userInfoExtractor = AuthenticationExtractor(
    configuration,
    realmInfoService,
    clock
  )

  override def invokeBlock[A](
      request: Request[A],
      block: AuthenticatedRequest[A, Authentication] => Future[Result]
  ): Future[Result] = authenticate(request, block)

  private def authenticate[A](
      request: Request[A],
      block: AuthenticatedRequest[A, Authentication] => Future[Result]
  ): Future[Result] =
    userInfoExtractor.extract(request) match
      case Some(auth) if !auth.expired =>
        block(new AuthenticatedRequest(auth, request))
      case Some(auth) if auth.expired =>
        refreshTokensAndContinue(request, auth, block)
      case None => Future.successful(Unauthenticated(configuration).respond(request))
      case _    => throw new Exception("Unexpected!") // to calm the compiler

  /** If the request is for HTML/XHTML content assume it is an ordinary browser and try to refresh
    * the access token and return it in a new cookie.
    */
  private def refreshTokensAndContinue[A](
      request: Request[A],
      auth: Authentication,
      block: AuthenticatedRequest[A, Authentication] => Future[Result]
  ) =
    if request.accepts(MimeTypes.HTML) || request.accepts(MimeTypes.XHTML) then
      sessionCache.cache
        .get[String](auth.identity)
        .filter(_.isDefined)
        .map(_.get)
        .flatMap(refreshAccessToken)
        .flatMap {
          case (keycloakTokenResponse, Some(auth)) =>
            block(new AuthenticatedRequest[A, Authentication](auth, request))
              .map(_.withCookies(Cookie(cookieName, keycloakTokenResponse.accessToken)))
          case _ => Future.successful(Unauthenticated(configuration).respond(request))
        }
        .recover { case e: Throwable =>
          logger.warn("Denying access", e)
          Unauthenticated(configuration).respond(request)
        }
    else Future.successful(Unauthenticated(configuration).respond(request))

  /** Calls the Oauth2 Token endpoint to retrieve new access and refresh tokens. The newly retrieved
    * access is then propagated to the action and the action response is augmented with a new cookie
    * value.
    *
    * @param refreshToken
    *   refresh token pulled from session cache
    */
  private def refreshAccessToken(
      refreshToken: String
  ): Future[(KeycloakTokenResponse, Option[Authentication])] =
    keycloakTokens
      .refreshTokens(refreshToken)
      .map { newAuth =>
        val decoded = userInfoExtractor.decodeJwt(newAuth.accessToken)
        (newAuth, decoded)
      }
