package application.security

import play.api.Configuration
import play.api.libs.ws.{WSAuthScheme, WSClient}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class SecurityController(
    cc: ControllerComponents,
    cfg: Configuration,
    ws: WSClient
) extends AbstractController(cc) {

  private val configuration =
    cfg.get[Oauth2OidcConfiguration]("security.oauth2_oidc")

  def oauthCallback: Action[AnyContent] = Action { request =>
    request.queryString
      .get("code")
      .flatMap(_.headOption)
      .flatMap(fetchToken)
      .map { tokenResponse =>

        Ok("")
      }
      .getOrElse(BadRequest("code query parameter is missing"))
  }

  private def fetchToken(code: String): Option[KeycloakTokenResponse] = {
    val tokenResponse = ws
      .url(configuration.tokenUrl)
      .withAuth(
        configuration.clientId,
        configuration.clientSecret,
        WSAuthScheme.BASIC
        )
      .post(
        Map(
          "code" -> Seq(code),
          "redirect_uri" -> Seq(configuration.redirectUri),
          "grant_type" -> Seq("authorization_code")
          )
        )
      .map { response => response.json.validate[KeycloakTokenResponse] }

    Await.result(tokenResponse, 5.seconds).asOpt
  }
}
