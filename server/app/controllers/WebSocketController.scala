package controllers

import javax.inject._
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import akka.actor.ActorSystem
import akka.stream._
import akka.actor.ActorRef
import actors.client

import play.api.libs.json.JsValue

class WebSocketController @Inject()(@Named("clientSupervisor") clientSupervisor: ActorRef)
                                   (implicit system: ActorSystem, materializer: Materializer) extends Controller {

  def socket = WebSocket.accept[JsValue, JsValue] { request =>
    ActorFlow.actorRef(out => client.WebSocketActor.props(clientSupervisor, out))
  }
}
