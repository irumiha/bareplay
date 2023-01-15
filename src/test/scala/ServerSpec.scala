import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play._
import play.api.mvc.Results
import play.api.test.Helpers._
import play.api.test.WsTestClient
import testsetup.{AllAppContainersTest, PostgresContainerTest}

class ServerSpec
    extends PlaySpec
    with BaseOneServerPerSuite
    with AllAppContainersTest
    with Results
    with ScalaFutures
    with IntegrationPatience {

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
}
