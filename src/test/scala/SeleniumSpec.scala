import org.scalatestplus.play._
import testsetup.TestedApplicationFactory

class SeleniumSpec
    extends PlaySpec
    with BaseOneServerPerTest
    with OneBrowserPerTest
    with TestedApplicationFactory
    with HtmlUnitFactory {

  "SeleniumSpec" should {

    "work from within a browser" in {

      go to ("http://localhost:" + port)

      pageSource must include("Welcome to Play!")
    }
  }
}
