package application.security

import play.api.libs.json.{Format, Json}

case class KeycloakTokenResponse(
    accessToken: String,
    expiresIn: Int,
    refreshExpiresIn: Int,
    refreshToken: String,
    tokenType: String,
    idToken: String,
    notBeforePolicy: Int,
    sessionState: String,
    scope: String
)

object KeycloakTokenResponse {
  implicit val serde: Format[KeycloakTokenResponse] = Json.format[KeycloakTokenResponse]
}
