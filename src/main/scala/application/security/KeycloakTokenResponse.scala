package application.security

import play.api.libs.json.JsonConfiguration.Aux
import play.api.libs.json._

case class KeycloakTokenResponse(
    accessToken: String,
    expiresIn: Int,
    refreshExpiresIn: Int,
    refreshToken: String,
    tokenType: String,
    idToken: String,
    sessionState: String,
    scope: String
)

object KeycloakTokenResponse {
  implicit val config: Aux[Json.MacroOptions]      = JsonConfiguration(JsonNaming.SnakeCase)
  implicit val serde: Reads[KeycloakTokenResponse] = Json.reads[KeycloakTokenResponse]
}
