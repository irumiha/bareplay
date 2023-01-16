package application.security

import pdi.jwt.{JwtAlgorithm, JwtJson}
import play.api.Configuration
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.libs.json.JsObject
import play.api.mvc.Security.AuthenticatedBuilder
import play.api.mvc._

import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/** Extracts authentication information from the incoming request.
  *
  * @param config
  *   Application configuration
  * @param realmInfoService
  *   Access to realm info endpoint. Really needed just for the JWT issuer public key
  */
case class AuthenticationExtractor(config: Configuration, realmInfoService: RealmInfoService)
    extends play.api.Logging {
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

  private def decodeJwt(jwtValue: String): Option[Authentication] = {
    val authentication =
      JwtJson
        .decodeJson(jwtValue, realmInfoService.realmInfo.publicKey, Seq(JwtAlgorithm.RS256))
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
    ec: ExecutionContext,
    realmInfoService: RealmInfoService
) extends AuthenticatedBuilder[Authentication](
      userinfo = AuthenticationExtractor(configuration, realmInfoService).extract,
      defaultParser = cc.parsers.defaultBodyParser,
      onUnauthorized = Unauthenticated(configuration).respond
    )(
      ec
    )
