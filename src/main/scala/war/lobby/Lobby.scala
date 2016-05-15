package war.lobby

import akka.actor.{ActorRef, Actor}

// Queries
case object Join
case object Leave
// Replies
case object Joined
case class OpponentFound(opponent: ActorRef)
case object Left
case object NotInQueue

class Lobby extends Actor with akka.actor.ActorLogging {

  val waitingQueue = new scala.collection.mutable.Queue[ActorRef]

  def receive = {
    case Join =>
      log.info("Received join message from {}.", sender)
      if (waitingQueue.isEmpty) {
        waitingQueue.enqueue(sender)
        sender ! Joined
      } else {
        val opponent = waitingQueue.dequeue
        sender ! OpponentFound(opponent)
        opponent ! OpponentFound(sender)
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
