package application.security

import com.softwaremill.tagging.@@
import pdi.jwt.{JwtAlgorithm, JwtJson, JwtOptions}
import play.api.Configuration
import play.api.cache.AsyncCacheApi
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.libs.json.JsObject
import play.api.mvc.Security.AuthenticatedRequest
import play.api.mvc._

import java.time.{Clock, Instant}
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/** Extracts authentication information from the incoming request.
  *
  * @param config
  *   Application configuration
  * @param realmInfoService
  *   Access to realm info endpoint. Really needed just for the JWT issuer public key
  */
case class AuthenticationExtractor(
    config: Configuration,
    realmInfoService: RealmInfoService,
    clock: Clock
) extends play.api.Logging {
  private val cookieName    = config.getOptional[String]("security.session_cookie_name")
  private val allowedIssuer = config.get[String]("security.oauth2_oidc.token_issuer")

  private def extractFromCookie(request: RequestHeader) = {
    cookieName
      .flatMap(request.cookies.get)
      .map(_.value)
      .flatMap(decodeJwt)
  }

  private def extractFromAuthorization(request: RequestHeader) = {
    request.headers
      .get(HeaderNames.AUTHORIZATION)
      .flatMap(tokenValueFromHeader)
      .flatMap(decodeJwt)
  }

  private def tokenValueFromHeader(authorization: String): Option[String] = {
    authorization.split("Bearer ").toList.drop(1).headOption
  }

  private[security] def decodeJwt(jwtValue: String): Option[Authentication] = {
    val authentication =
      JwtJson
        .decodeJson(
          jwtValue,
          realmInfoService.realmInfo.publicKey,
          Seq(JwtAlgorithm.RS256),
          JwtOptions.DEFAULT.copy(expiration = false)
        )
        .filter(c => (c \ "iss").as[String] == allowedIssuer)
        .map(claimsToAuthentication)

    authentication match {
      case Success(auth) => Some(auth)
      case Failure(ex) =>
        logger.error(ex.getMessage)
        None
    }
  }

  private def claimsToAuthentication = { claimsJson: JsObject =>
    Authentication(
      identity = (claimsJson \ "sub").as[String],
      username = (claimsJson \ "preferred_username").as[String],
      firstName = (claimsJson \ "given_name").asOpt[String].filterNot(_.isEmpty),
      familyName = (claimsJson \ "family_name").asOpt[String].filterNot(_.isEmpty),
      roles = (claimsJson \ "realm_access" \ "roles").as[Set[String]],
      expired = !Instant.ofEpochSecond((claimsJson \ "exp").as[Long]).isAfter(Instant.now(clock)),
      attributes = Map.empty
    )
  }

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

}

case class Unauthenticated(configuration: Configuration) extends Status {
  private val nextUrlCookieName = configuration.get[String]("security.login_redirect_cookie_name")
  private val authUri           = configuration.get[String]("security.oauth2_oidc.auth_url")
  private val clientId          = configuration.get[String]("security.oauth2_oidc.client_id")
  private val redirectUri       = configuration.get[String]("security.oauth2_oidc.redirect_uri")

  private val redirectParams = {
    Map(
      "scope"         -> Seq("openid"),
      "response_type" -> Seq("code"),
      "client_id"     -> Seq(clientId),
      "redirect_uri"  -> Seq(redirectUri),
      "state"         -> Seq(UUID.randomUUID().toString)
    )
  }

  /** If the request was for a HTML page redirect to login page on Oauth2 provider, otherwise return
    * Unauthorized (http status 401)
    * @param request
    *   Incoming request
    * @return
    *   Redirect response or http status 401
    */
  def respond(request: RequestHeader): Result = {
    if (request.accepts(MimeTypes.HTML) || request.accepts(MimeTypes.XHTML)) {
      Results
        .Redirect(authUri, redirectParams, SEE_OTHER)
        .withCookies(
          Cookie(nextUrlCookieName, request.uri)
        )
    } else {
      Results.Unauthorized("")
    }
  }
}

class UserAuthenticatedBuilder(
    cc: ControllerComponents,
    configuration: Configuration,
    realmInfoService: RealmInfoService,
    sessionCache: AsyncCacheApi @@ Authentication,
    keycloakTokens: KeycloakTokens,
    clock: Clock
)(override implicit val executionContext: ExecutionContext)
    extends ActionBuilder[({ type R[A] = AuthenticatedRequest[A, Authentication] })#R, AnyContent] {
  override def parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser

  private val cookieName = configuration.get[String]("security.session_cookie_name")
  private val userInfoExtractor = AuthenticationExtractor(
    config = configuration,
    realmInfoService = realmInfoService,
    clock = clock
  )

  override def invokeBlock[A](
      request: Request[A],
      block: AuthenticatedRequest[A, Authentication] => Future[Result]
  ): Future[Result] = authenticate(request, block)

  private def authenticate[A](
      request: Request[A],
      block: AuthenticatedRequest[A, Authentication] => Future[Result]
  ): Future[Result] = {
    userInfoExtractor.extract(request) match {
      case Some(auth) if !auth.expired =>
        block(new AuthenticatedRequest(auth, request))
      case Some(auth) if auth.expired =>
        if (request.accepts(MimeTypes.HTML) || request.accepts(MimeTypes.XHTML)) {
          sessionCache
            .get[String](auth.identity)
            .flatMap {
              case Some(refreshToken) =>
                refreshAccessTokenAndCallAction(request, refreshToken, block)
              case None => Future.successful(Unauthenticated(configuration).respond(request))
            }
        } else {
          Future.successful(Unauthenticated(configuration).respond(request))
        }
      case None => Future.successful(Unauthenticated(configuration).respond(request))
      case _ => throw new Exception("Unexpected!") // to calm the compiler
    }
  }

  /**
   * Calls the Oauth2 Token endpoint to retrieve new access and refresh tokens.
   * The newly retrieved access is then propagated to the action and the
   * action response is augmented with a new cookie value.
   *
   * @param request incoming request
   * @param refreshToken refresh token pulled from session cache
   * @param block action to execute with new Authentication
   * @return Future of result
   */
  private def refreshAccessTokenAndCallAction[A](
    request: Request[A],
    refreshToken: String,
    block: AuthenticatedRequest[A, Authentication] => Future[Result]
  ): Future[Result] = {
    keycloakTokens
      .refreshTokens(refreshToken)
      .flatMap { newAuth =>
        userInfoExtractor
          .decodeJwt(newAuth.accessToken)
          .map { authentication =>
            block(new AuthenticatedRequest(authentication, request))
              .map(_.withCookies(Cookie(cookieName, newAuth.accessToken)))
          }
          .getOrElse(Future.successful(Unauthenticated(configuration).respond(request)))
      }
  }
}

object UserAuthenticatedBuilder {
  def build(
      cc: ControllerComponents,
      configuration: Configuration,
      realmInfoService: RealmInfoService,
      sessionCache: AsyncCacheApi @@ Authentication,
      keycloakTokens: KeycloakTokens,
      clock: Clock
  )(implicit executionContext: ExecutionContext): UserAuthenticatedBuilder =
    new UserAuthenticatedBuilder(
      cc,
      configuration,
      realmInfoService,
      sessionCache,
      keycloakTokens,
      clock
    )
}
