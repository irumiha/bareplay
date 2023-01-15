package application.security

import pdi.jwt.{JwtAlgorithm, JwtJson}
import play.api.Configuration
import play.api.http.{MimeTypes, Status}
import play.api.mvc.Security.AuthenticatedBuilder
import play.api.mvc._

import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

case class AuthenticationExtractor(config: Configuration) extends play.api.Logging {
  private val cookieName    = config.get[String]("security.session_cookie_name")
  private val allowedIssuer = config.get[String]("security.oauth2_oidc.token_issuer")
  private val jwtPublicKey  = config.get[String]("security.oauth2_oidc.jwt_signing_public_key")

  def extract(request: RequestHeader): Option[Authentication] = {
    request.cookies.get(cookieName).flatMap { cookie =>
      JwtJson
        .decodeJson(cookie.value, jwtPublicKey, Seq(JwtAlgorithm.RS256))
        .filter(c => (c \ "iss").as[String] == allowedIssuer)
        .map { claimsJson =>
          Authentication(
            identity = (claimsJson \ "sub").as[String],
            username = (claimsJson \ "preferred_username").as[String],
            firstName = (claimsJson \ "given_name").asOpt[String].filterNot(_.isEmpty),
            familyName = (claimsJson \ "family_name").asOpt[String].filterNot(_.isEmpty),
            roles = (claimsJson \ "realm_access" \ "roles").as[Set[String]],
            attributes = Map.empty
          )
        } match {
        case Success(auth) => Some(auth)
        case Failure(ex) =>
          logger.error(ex.getMessage)
          None
      }
    }
  }
}

case class Unauthenticated(configuration: Configuration) extends Status {
  private val authUri           = configuration.get[String]("security.oauth2_oidc.auth_url")
  private val nextUrlCookieName = configuration.get[String]("security.login_redirect_cookie_name")
  private val clientId          = configuration.get[String]("security.oauth2_oidc.client_id")
  private val redirectUri       = configuration.get[String]("security.oauth2_oidc.redirect_uri")

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
        .Redirect(
          authUri,
          Map(
            "scope"         -> Seq("openid"),
            "response_type" -> Seq("code"),
            "client_id"     -> Seq(clientId),
            "redirect_uri"  -> Seq(redirectUri),
            "state"         -> Seq(UUID.randomUUID().toString)
          ),
          SEE_OTHER
        )
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
    ec: ExecutionContext
) extends AuthenticatedBuilder[Authentication](
      userinfo = AuthenticationExtractor(configuration).extract,
      defaultParser = cc.parsers.defaultBodyParser,
      onUnauthorized = Unauthenticated(configuration).respond
    )(ec)
