package application.security

import com.softwaremill.tagging.@@
import play.api.Configuration
import play.api.cache.AsyncCacheApi
import play.api.libs.json._
import play.api.libs.ws.{WSAuthScheme, WSClient}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

/**
 * Utility class with methods to fetch initial set of tokens using the authorization code
 * and to refresh the tokens usin the refresh token.
 */
class KeycloakTokens(
    cfg: Configuration,
    ws: WSClient,
    sessionCache: AsyncCacheApi @@ Authentication
) {
  private val configuration =
    cfg.get[Oauth2OidcConfiguration]("security.oauth2_oidc")

  def refreshTokens(refreshToken: String)(implicit ec: ExecutionContext): Future[KeycloakTokenResponse] = {
    queryTokenEndpoint(
      Map(
        "refresh_token" -> Seq(refreshToken),
        "redirect_uri"  -> Seq(configuration.redirectUri),
        "grant_type"    -> Seq("refresh_token"),
        "client_id"     -> Seq(configuration.clientId)
      )
    )
  }

  def fetchInitialTokens(code: String)(implicit ec: ExecutionContext): Future[KeycloakTokenResponse] = {
    queryTokenEndpoint(
      Map(
        "code"         -> Seq(code),
        "redirect_uri" -> Seq(configuration.redirectUri),
        "grant_type"   -> Seq("authorization_code")
      )
    )
  }

  private def queryTokenEndpoint(
      postParams: Map[String, Seq[String]]
  )(implicit ec: ExecutionContext): Future[KeycloakTokenResponse] = {
    ws
      .url(configuration.tokenUrl)
      .withAuth(
        configuration.clientId,
        configuration.clientSecret,
        WSAuthScheme.BASIC
      )
      .withRequestTimeout(5.seconds)
      .post(postParams)
      .flatMap { response =>
        if (response.status == 200) {
          response.json.validate[KeycloakTokenResponse] match {
            case JsSuccess(value, _) =>
              sessionCache
                .set(value.accessToken, value.refreshToken, value.refreshExpiresIn.seconds)
                .map(_ => value)
            case JsError(errors) =>
              Future.failed[KeycloakTokenResponse](new Exception(errors.toString()))
          }
        } else {
          Future.failed[KeycloakTokenResponse](new Exception(response.body))
        }
      }
  }

}
