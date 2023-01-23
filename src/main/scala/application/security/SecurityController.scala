package application.security

import play.api.Configuration
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class SecurityController(
    cc: ControllerComponents,
    cfg: Configuration,
    keycloakTokens: KeycloakTokens
) extends AbstractController(cc)
    with play.api.Logging {
  implicit val ec: ExecutionContext = cc.executionContext

  private val nextUrlCookieName = cfg.get[String]("security.login_redirect_cookie_name")
  private val sessionCookieName = cfg.get[String]("security.session_cookie_name")

  /**
   * Callback endpoint that wraps up the Oauth2 dance. This is where Keycloak sends the
   * user after they successfully authenticate. Payload is authorization code we then
   * use to get access and refresh tokens.
   *
   * @return
   */
  def oauthCallback: Action[AnyContent] = Action.async { request =>
    val redirectTarget = request.cookies.get(nextUrlCookieName)
    request.queryString
      .get("code")
      .flatMap(_.headOption) match {
      case Some(value) =>
        keycloakTokens.fetchInitialTokens(value).map { tokenResponse =>
          redirectTarget
            .map { c =>
              Redirect(c.value, FOUND).withCookies(
                Cookie(sessionCookieName, tokenResponse.accessToken)
              )
            }
            .getOrElse(Ok(""))
        }(cc.executionContext)
      case None => Future.successful(BadRequest("code query parameter is missing"))
    }
  }

}
