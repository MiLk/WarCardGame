import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.ws.WebSocket
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.{TestServer, Helpers, WsTestClient}
import scala.compat.java8.FutureConverters
import scala.concurrent.duration._

class WebSocketSpec extends PlaySpec with ScalaFutures {

  "WebSocketController" should {

    "should accept a websocket flow" in WsTestClient.withClient { client =>
      lazy val port: Int = Helpers.testServerPort
      val app = new GuiceApplicationBuilder().build()
      Helpers.running(TestServer(port, app)) {
        val myPublicAddress = s"localhost:$port"
        val serverURL = s"ws://$myPublicAddress/ws"

        val asyncHttpClient: AsyncHttpClient = client.underlying[AsyncHttpClient]

        val webSocketClient = new WebSocketClient(asyncHttpClient)

        val origin = serverURL
        val listener = new WebSocketClient.LoggingListener
        val completionStage = webSocketClient.call(serverURL, origin, listener)
        val f = FutureConverters.toScala(completionStage)

        whenReady(f, timeout = Timeout(1.second)) { webSocket =>
          webSocket mustBe a[WebSocket]
        }
      }
    }

  }

}
