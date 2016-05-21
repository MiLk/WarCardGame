package war.game

import akka.actor.{ActorRef, Actor, Props}

case object GameFound
case object GameStartConfirmation
case object GameStart

object Game {
  def props(players: Set[ActorRef]): Props = Props(new Game(players))
}

class Game(players: Set[ActorRef]) extends Actor with akka.actor.ActorLogging {

  players.foreach(_ ! GameFound)

  def receive = waitingForStart(Set.empty[ActorRef])

  def waitingForStart(confirmed: Set[ActorRef]): Actor.Receive = {
    case GameStartConfirmation =>
      if (!confirmed.contains(sender)) {
        val newConfirmed = confirmed + sender
        if (players.diff(newConfirmed).isEmpty) {
          players.foreach(_ ! GameStart)
          context.become(inProgress)
        }
        else context.become(waitingForStart(newConfirmed))
      }
  }

  def inProgress: Actor.Receive = {
    case _ =>
  }
}
