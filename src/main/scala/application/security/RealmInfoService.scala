package application.security

import play.api.libs.json.{JsError, JsSuccess}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

/**
 * Fetch (only once!) information from the realm endpoint.
 *
 * Currently used to retrieve the JWT token issuer public key.
 */
class RealmInfoService(cfg: Oauth2OidcConfiguration, ws: WSClient) {
  lazy val realmInfo: RealmInfo = {
    Await.result(requestRealmInfo, 6.seconds)
  }

  private def requestRealmInfo = {
    ws
      .url(cfg.tokenIssuer)
      .withRequestTimeout(5.seconds)
      .get()
      .flatMap(parseRealmInfo)
  }

  private def parseRealmInfo = { resp: WSResponse =>
    resp.json.validate[RealmInfo] match {
      case JsSuccess(value, _) => Future.successful(value)
      case JsError(errors) =>
        Future.failed[RealmInfo](new Exception(errors.toString()))
    }
  }
}
