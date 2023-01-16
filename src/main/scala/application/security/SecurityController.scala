package application.security

import play.api.Configuration
import play.api.libs.json.{JsError, JsSuccess}
import play.api.libs.ws.{WSAuthScheme, WSClient}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class SecurityController(
    cc: ControllerComponents,
    cfg: Configuration,
    ws: WSClient
) extends AbstractController(cc)
    with play.api.Logging {

  private val configuration =
    cfg.get[Oauth2OidcConfiguration]("security.oauth2_oidc")
  private val nextUrlCookieName = cfg.get[String]("security.login_redirect_cookie_name")
  private val sessionCookieName = cfg.get[String]("security.session_cookie_name")

  def oauthCallback: Action[AnyContent] = Action.async { request =>
    val redirectTarget = request.cookies.get(nextUrlCookieName)
    request.queryString
      .get("code")
      .flatMap(_.headOption) match {
      case Some(value) =>
        fetchToken(value).map { tokenResponse =>
          redirectTarget
            .map { c =>
              Redirect(c.value, FOUND).withCookies(
                Cookie(sessionCookieName, tokenResponse.accessToken)
              )
            }
            .getOrElse(Ok(""))
        }
      case None => Future.successful(BadRequest("code query parameter is missing"))
    }
  }


  private def fetchToken(code: String): Future[KeycloakTokenResponse] = {
    ws
      .url(configuration.tokenUrl)
      .withAuth(
        configuration.clientId,
        configuration.clientSecret,
        WSAuthScheme.BASIC
      )
      .withRequestTimeout(5.seconds)
      .post(
        Map(
          "code"         -> Seq(code),
          "redirect_uri" -> Seq(configuration.redirectUri),
          "grant_type"   -> Seq("authorization_code")
        )
      )
      .flatMap { response =>
        if (response.status == 200) {
          response.json.validate[KeycloakTokenResponse] match {
            case JsSuccess(value, _) => Future.successful(value)
            case JsError(errors) =>
              Future.failed[KeycloakTokenResponse](new Exception(errors.toString()))
          }
        } else {
          Future.failed[KeycloakTokenResponse](new Exception(response.body))
        }
      }
  }
}
