import org.scalatestplus.play._
import testsetup.PostgresTest

class SeleniumSpec
    extends PlaySpec
    with BaseOneServerPerTest
    with OneBrowserPerTest
    with HtmlUnitFactory
    with PostgresTest {

  "SeleniumSpec" should {

    "work from within a browser" in {

      go to ("http://localhost:" + port)

      pageSource must include("Welcome to Play!")
    }
  }
}
