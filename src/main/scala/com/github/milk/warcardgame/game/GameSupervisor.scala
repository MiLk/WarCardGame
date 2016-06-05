package com.github.milk.warcardgame.game

import akka.actor.{Actor, ActorRef, Props}

object GameSupervisor {
  def props(gameActorProps: Set[ActorRef] => Props): Props = Props(new GameSupervisor(gameActorProps))

  case class CreateGame(players: Set[ActorRef])

}

class GameSupervisor(gameActorProps: Set[ActorRef] => Props) extends Actor with akka.actor.ActorLogging {

  import GameSupervisor._

  def receive = {
    case CreateGame(players) => context.actorOf(gameActorProps(players))
  }
}
