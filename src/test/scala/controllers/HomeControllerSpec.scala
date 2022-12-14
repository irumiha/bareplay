package controllers

import testsetup.TestedApplicationFactory
import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._

class HomeControllerSpec
    extends PlaySpec
    with BaseOneAppPerTest
    with TestedApplicationFactory {

  "Routes" should {
    "send 404 on a bad request" in {
      route(app, FakeRequest(GET, "/boum")).map(status(_)) mustBe Some(
        NOT_FOUND
      )
    }
  }

  "HomeController GET" should {

    "render the index page from the router" in {
      val request = FakeRequest(GET, "/")
      val home    = route(app, request).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include("Welcome to Play")
    }
  }
}
