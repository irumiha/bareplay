package application.security

import play.api.libs.json.JsonConfiguration.Aux
import play.api.libs.json.{Json, JsonConfiguration, JsonNaming, Reads}

case class RealmInfo(
    realm: String,
    publicKey: String,
    tokenService: String,
    accountService: String,
    tokensNotBefore: Int
)

object RealmInfo {
  implicit val config: Aux[Json.MacroOptions] = JsonConfiguration(JsonNaming.SnakeCase)
  implicit val serde: Reads[RealmInfo]        = Json.reads[RealmInfo]
}
