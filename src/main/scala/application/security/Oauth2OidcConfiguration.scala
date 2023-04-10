package application.security

import com.typesafe.config.Config
import play.api.ConfigLoader

case class Oauth2OidcConfiguration(
    clientId: String,
    clientSecret: String,
    authUrl: String,
    tokenUrl: String,
    redirectUri: String,
    tokenIssuer: String
)

object Oauth2OidcConfiguration:
  implicit val configLoader: ConfigLoader[Oauth2OidcConfiguration] =
    (rootConfig: Config, path: String) =>
      val oauthConfig = rootConfig.getConfig(path)

      Oauth2OidcConfiguration(
        clientId = oauthConfig.getString("client_id"),
        clientSecret = oauthConfig.getString("client_secret"),
        authUrl = oauthConfig.getString("auth_url"),
        tokenUrl = oauthConfig.getString("token_url"),
        redirectUri = oauthConfig.getString("redirect_uri"),
        tokenIssuer = oauthConfig.getString("token_issuer")
      )
