package war

import akka.actor.{Props, Actor}
import war.lobby.Lobby
import war.client.Client

object Main {
  def main(args: Array[String]): Unit = {
    akka.Main.main(Array(classOf[Main].getName))
  }
}

class Main extends Actor with akka.actor.ActorLogging {
  val lobbyActor = context.actorOf(Props[Lobby], "lobby")

  val client1 = context.actorOf(Props[Client], "clientA")
  val client2 = context.actorOf(Props[Client], "clientB")

  def receive = {
    case client.Connect => sender ! client.Connected(lobbyActor)
    case _ => log.error("Main received unhandled message")
  }
}
