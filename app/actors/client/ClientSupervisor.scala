package actors.client

import akka.actor.{Terminated, Actor, Props, ActorRef}
import scala.collection.mutable

object ClientSupervisor {
  def props(lobby: ActorRef) = Props(new ClientSupervisor(lobby))

  case class Get(clientId: String)

}

class ClientSupervisor(lobby: ActorRef) extends Actor with akka.actor.ActorLogging {

  import ClientSupervisor._

  val clients = mutable.Map.empty[String, ActorRef]

  def receive = {
    case Get(clientId) =>
      if (clients.contains(clientId)) {
        sender ! clients(clientId)
      } else {
        val client = context.actorOf(Client.props(lobby), clientId)
        context.watch(client)
        sender ! client
        clients += (clientId -> client)
      }
    case Terminated(actor: ActorRef) =>
      for {
        client <- clients if client._2 == actor
      } clients -= client._1
  }
}
