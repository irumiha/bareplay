package controllers

import org.scalatestplus.play.*
import play.api.test.Helpers.*
import play.api.test.*
import testsetup.PostgresTest

class HomeControllerSpec extends PlaySpec with BaseOneAppPerTest with PostgresTest:

  "Routes" should {
    "respond with a status 404 on a request to unknown URL" in {
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
