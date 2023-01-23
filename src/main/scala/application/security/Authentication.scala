package application.security

import play.api.libs.json.{Format, Json}

case class Authentication(
    identity: String, // sub field from JWT
    username: String,
    firstName: Option[String],
    familyName: Option[String],
    roles: Set[String],
    attributes: Map[String, String],
    expired: Boolean = false
)
object Authentication {
  implicit val serde: Format[Authentication] = Json.format[Authentication]
}
