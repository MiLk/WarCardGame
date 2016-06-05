package com.github.milk.warcardgame

import akka.actor.{Props, Actor,Terminated}

object Main {
  def main(args: Array[String]): Unit = {
    akka.Main.main(Array(classOf[Main].getName))
  }
}

class Main extends Actor with akka.actor.ActorLogging {
  import client.Client._

  // TODO Add a service discovery service
  val gameSupervisor = context.actorOf(Props[game.GameSupervisor], "gameSupervisor")
  val lobbyActor = context.actorOf(lobby.Lobby.props(gameSupervisor), "lobby")

  val client1 = context.actorOf(Props[client.Client], "clientA")
  val client2 = context.actorOf(Props[client.Client], "clientB")
  context.watch(client1)
  context.watch(client2)
  var terminated = 0

  def receive = {
    case Connect => sender ! Connected(lobbyActor)
    case Terminated(child) =>
      if (terminated == 0) terminated = 1
      else context.stop(self)
    case _ => log.error("Main received unhandled message")
  }
}
