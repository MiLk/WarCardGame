package war

import akka.actor.{Props, Actor}

object Main {
  def main(args: Array[String]): Unit = {
    akka.Main.main(Array(classOf[Main].getName))
  }
}

class Main extends Actor with akka.actor.ActorLogging {
  // TODO Add a service discovery service
  val gameSupervisor = context.actorOf(Props[game.GameSupervisor], "gameSupervisor")
  val lobbyActor = context.actorOf(lobby.Lobby.props(gameSupervisor), "lobby")

  val client1 = context.actorOf(Props[client.Client], "clientA")
  val client2 = context.actorOf(Props[client.Client], "clientB")

  def receive = {
    case client.Connect => sender ! client.Connected(lobbyActor)
    case _ => log.error("Main received unhandled message")
  }
}
