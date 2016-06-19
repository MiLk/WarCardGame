package actors.client

import akka.actor.{Actor, ActorRef, Props}

object Client {
  def props(lobby: ActorRef) = Props(new Client(lobby))

  case object JoinQueue
  case object QueueJoined
}

class Client(lobby: ActorRef) extends Actor with akka.actor.ActorLogging {
  import actors.lobby.Lobby
  import actors.game.Game._
  import Client._

  def receive = standBy

  // The client is not in the queue yet
  def standBy: Actor.Receive = {
    case JoinQueue =>
      log.debug("Join Queue")
      lobby ! Lobby.Join
      context.become(waitingForQueueConfirmation(sender))
  }

  def waitingForQueueConfirmation(replyTo: ActorRef): Actor.Receive = {
    case QueueJoined =>
      log.debug("Queue joined")
      context.become(waitingForOpponent)
      replyTo ! "You have joined the waiting queue."
    case JoinQueue =>
      replyTo ! "Waiting for confirmation from the lobby."
  }

  // The client is in the queue and waiting for an opponent
  def waitingForOpponent: Actor.Receive = {
    case GameFound =>
      log.debug("Game found")
      context.become(waitingForStart)
      sender ! GameStartConfirmation
    case JoinQueue =>
      sender ! "You are already in queue."
  }

  // The game has been found and waiting for it to start
  def waitingForStart: Actor.Receive = {
    case GameStart =>
      context.become(inProgress)
      sender ! Draw
    case JoinQueue =>
      sender ! "You can't do that while waiting for the game to start."
  }

  // The game is in progress
  def inProgress: Actor.Receive = {
    case NextTurn =>
      sender ! Draw
    case GameOver =>
      log.info("{} lost the game", self)
      context.stop(self)
    case Victory =>
      log.info("{} won the game", self)
      context.stop(self)
    case JoinQueue =>
      sender ! "You can't do that while a game is in progress."
  }
}
