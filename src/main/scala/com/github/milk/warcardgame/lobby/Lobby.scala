package com.github.milk.warcardgame.lobby

import akka.actor.{ActorRef, Actor, Props}

object Lobby {
  def props(gameSupervisor: ActorRef): Props = Props(new Lobby(gameSupervisor))
  // Queries
  case object Join
  case object Leave
  // Notifications
  case object Joined
  case object Left
  case object NotInQueue
}

class Lobby(gameSupervisor: ActorRef) extends Actor with akka.actor.ActorLogging {
  import Lobby._
  import com.github.milk.warcardgame.game.GameSupervisor._

  val waitingQueue = new scala.collection.mutable.Queue[ActorRef]

  def receive = {
    case Join =>
      log.info("Received join message from {}.", sender)
      if (waitingQueue.isEmpty) {
        waitingQueue.enqueue(sender)
        sender ! Joined
      } else if (!waitingQueue.contains(sender)) {
        // TODO add ack/retry
        gameSupervisor ! CreateGame(Set(sender, waitingQueue.dequeue))
      }
    case Leave =>
      if (waitingQueue.contains(sender)) {
        waitingQueue.dequeueAll(_ == sender)
        sender ! Left
      } else {
        sender ! NotInQueue
      }
  }
}
