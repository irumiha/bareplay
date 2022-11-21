package application.security

import play.api.libs.json.Json

case class Authentication (
    identity: String,
    roles: Set[String],
    attributes: Map[String, String]
)
object Authentication {
  implicit val serde = Json.format[Authentication]
}
