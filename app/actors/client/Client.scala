package actors.client

import akka.actor.{Actor, ActorRef, Props}

object Client {
  def props(lobby: ActorRef) = Props(new Client(lobby))

  case object JoinQueue
  case object QueueJoined
  case object Draw
}

class Client(lobby: ActorRef) extends Actor with akka.actor.ActorLogging {
  import actors.lobby.Lobby
  import actors.game.Game
  import Client._

  def receive = standBy

  // The client is not in the queue yet
  def standBy: Actor.Receive = {
    case JoinQueue =>
      log.debug("Join Queue")
      lobby ! Lobby.Join
      context.become(waitingForQueueConfirmation(sender))
    case Draw =>
      sender ! "You must join the queue first."
  }

  def waitingForQueueConfirmation(replyTo: ActorRef): Actor.Receive = {
    case QueueJoined =>
      log.debug("Queue joined")
      context.become(waitingForOpponent)
      replyTo ! "You have joined the waiting queue."
    case JoinQueue =>
      replyTo ! "Waiting for confirmation from the lobby."
    case Draw =>
      sender ! "No game has been found yet."
  }

  // The client is in the queue and waiting for an opponent
  def waitingForOpponent: Actor.Receive = {
    case Game.GameFound =>
      log.debug("Game found")
      context.become(waitingForStart)
      sender ! Game.GameStartConfirmation
    case JoinQueue =>
      sender ! "You are already in queue."
    case Draw =>
      sender ! "No game has been found yet."
  }

  // The game has been found and waiting for it to start
  def waitingForStart: Actor.Receive = {
    case Game.GameStart =>
      context.become(inProgress(sender))
      sender ! Draw
    case JoinQueue =>
      sender ! "You can't do that while waiting for the game to start."
    case Draw =>
      sender ! "The game has not started yet."
  }

  // The game is in progress
  def inProgress(gameActor: ActorRef): Actor.Receive = {
    case Draw =>
      gameActor ! Game.Draw
      context.become(waitingNextTurn(sender))
    case JoinQueue =>
      sender ! "You can't do that while a game is in progress."
  }

  def waitingNextTurn(replyTo: ActorRef): Actor.Receive = {
    case Game.NextTurn(drawnCards) =>
      replyTo ! drawnCards.map {
        case (name, card) => s"$name has drawn $card"
      }.mkString("\n")
      context.become(inProgress(sender))
    case Game.GameOver =>
      replyTo ! s"$self lost the game"
      context.stop(self)
    case Game.Victory =>
      replyTo ! s"$self won the game"
      context.stop(self)
    case JoinQueue =>
      sender ! "You can't do that while a game is in progress."
    case Draw =>
      sender ! "Still waiting for the other player to Draw."
  }
}
