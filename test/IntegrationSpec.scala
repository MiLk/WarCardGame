import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
class IntegrationSpec extends PlaySpec with OneServerPerTest with OneBrowserPerTest with HtmlUnitFactory {

  "Application" should {

    /*
    Disabled this test as the browser doesn't support ES6
    "work from within a browser" in {

      go to ("http://localhost:" + port)

      pageSource must include ("Connection in progress...")
    }
    */
  }
}
