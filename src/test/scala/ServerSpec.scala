import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.*
import play.api.mvc.Results
import play.api.test.Helpers.*
import play.api.test.WsTestClient
import testsetup.AllContainersTest

class ServerSpec
    extends PlaySpec
    with BaseOneServerPerSuite
    with AllContainersTest
    with Results
    with ScalaFutures
    with IntegrationPatience:

  override val realmName = "test-realm"

  "Server query should" should {
    "work" in {
      WsTestClient.withClient { implicit client =>
        whenReady(wsUrl("/").get()) { response =>
          response.status mustBe OK
        }
      }
    }
  }
