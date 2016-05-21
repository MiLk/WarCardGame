package war.lobby

import akka.actor.{ActorRef, Actor, Props}

// Queries
case object Join
case object Leave

// Replies
case object Joined
case object Left
case object NotInQueue

object Lobby {
  def props(gameSupervisor: ActorRef): Props = Props(new Lobby(gameSupervisor))
}

class Lobby(gameSupervisor: ActorRef) extends Actor with akka.actor.ActorLogging {

  val waitingQueue = new scala.collection.mutable.Queue[ActorRef]

  def receive = {
    case Join =>
      log.info("Received join message from {}.", sender)
      if (waitingQueue.isEmpty) {
        waitingQueue.enqueue(sender)
        sender ! Joined
      } else if (!waitingQueue.contains(sender)) {
        // TODO add ack/retry
        gameSupervisor ! war.game.CreateGame(Set(sender, waitingQueue.dequeue))
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
