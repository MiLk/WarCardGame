package actors.client

import akka.actor._
import play.api.libs.json._

import scala.collection.Seq

object WebSocketActor {
  def props(clientSupervisor: ActorRef, out: ActorRef) = Props(new WebSocketActor(clientSupervisor, out))

  sealed trait WebSocketReply

  case class Error(message: String, description: String) extends WebSocketReply {
    def toJsObject: JsObject =
      JsObject(Seq(
        "type" -> JsString("error"),
        "message" -> JsString(message),
        "description" -> JsString(description)
      ))
  }

  case class Success(fields: Seq[(String, JsValue)]) extends WebSocketReply {
    def toJsObject: JsObject = JsObject(Seq("type" -> JsString("success")) ++ fields)
  }

  def generateId : String = java.util.UUID.randomUUID.toString
}

class WebSocketActor(clientSupervisor: ActorRef, out: ActorRef) extends Actor with Stash {

  import WebSocketActor._
  import actors.client

  val id = generateId

  clientSupervisor ! client.ClientSupervisor.Get(id, self)

  def receive = {
    case actor: ActorRef if sender == clientSupervisor =>
      unstashAll()
      context.become(initialized(actor))
    case _ => stash()
  }

  def receiveMessagesFromWebSocket(clientActor: ActorRef) : Actor.Receive = {
    case msg: JsObject if msg.value.contains("action") =>
      val action = msg.value("action")
      action match {
        case JsString("join") => clientActor ! client.Client.JoinQueue
        case JsString("draw") => clientActor ! client.Client.Draw
        case _ => Error("Invalid format", "The action has not been recognized").toJsObject
      }
    case msg: JsObject =>
      out ! Error("Invalid format", "A JSON Object with an action key is expected").toJsObject
    case msg: JsValue =>
      out ! Error("Invalid format", "A JSON Object is expected").toJsObject
  }

  def receiveMessageFromActorSystem: Actor.Receive = {
    case msg: String => out ! JsString(msg)
  }

  def initialized(clientActor: ActorRef) = receiveMessagesFromWebSocket(clientActor) orElse receiveMessageFromActorSystem
}
