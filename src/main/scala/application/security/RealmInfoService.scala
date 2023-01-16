package application.security

import play.api.libs.json.{JsError, JsSuccess}
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class RealmInfoService(
    cfg: Oauth2OidcConfiguration,
    ws: WSClient
) {
  lazy val realmInfo: RealmInfo = {
    Await.result(
      ws
        .url(cfg.tokenIssuer)
        .withRequestTimeout(5.seconds)
        .get()
        .flatMap { resp =>
          resp.json.validate[RealmInfo] match {
            case JsSuccess(value, _) => Future.successful(value)
            case JsError(errors) =>
              Future.failed[RealmInfo](new Exception(errors.toString()))
          }
        },
      6.seconds
    )
  }
}
