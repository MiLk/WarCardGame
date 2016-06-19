package actors.lobby

import akka.actor.{ActorRef, Actor, Props}

object Lobby {
  def props(gameSupervisor: ActorRef): Props = Props(new Lobby(gameSupervisor))
  // Queries
  case object Join
  case object Leave
  // Notifications
  case object Left
  case object NotInQueue
}

class Lobby(gameSupervisor: ActorRef) extends Actor with akka.actor.ActorLogging {
  import Lobby._
  import actors.game.GameSupervisor._
  import actors.client.Client

  val waitingQueue = new scala.collection.mutable.Queue[ActorRef]

  def receive = {
    case Join =>
      log.debug("Received join message from {}.", sender)
      if (waitingQueue.isEmpty) {
        waitingQueue.enqueue(sender)
        sender ! Client.QueueJoined
      } else if (!waitingQueue.contains(sender)) {
        sender ! Client.QueueJoined
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
