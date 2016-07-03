package actors.client

import akka.actor.{Actor, ActorRef, Props}

object Client {
  def props(lobby: ActorRef, connection: ActorRef) = Props(new Client(lobby, connection))

  case object Connect
  case object JoinQueue
  case object QueueJoined
  case object Draw
}

class Client(lobby: ActorRef, connection: ActorRef) extends Actor {
  import actors.lobby.Lobby
  import actors.game.Game
  import Client._
  import Connection.Message

  def receive = standBy

  // The client is not in the queue yet
  def standBy: Actor.Receive = {
    case JoinQueue =>
      lobby ! Lobby.Join
      context.become(waitingForQueueConfirmation)
    case Draw =>
      sender ! "You must join the queue first."
  }

  // The client has requested to join the queue
  def waitingForQueueConfirmation: Actor.Receive = {
    case QueueJoined =>
      context.become(waitingForOpponent)
      connection ! Message("You have joined the waiting queue. Waiting for an opponent.", "InQueue")
    case JoinQueue =>
      connection ! "Waiting for confirmation from the lobby."
    case Draw =>
      sender ! "No game has been found yet."
  }

  // The client is in the queue and waiting for an opponent
  def waitingForOpponent: Actor.Receive = {
    case Game.GameFound =>
      context.become(waitingForStart)
      sender ! Game.GameStartConfirmation
      connection ! Message("An opponent has been found. Waiting for the game to start.", "InQueue")
    case JoinQueue =>
      sender ! "You are already in queue."
    case Draw =>
      sender ! "No game has been found yet."
  }

  // The game has been found and waiting for it to start
  def waitingForStart: Actor.Receive = {
    case Game.GameStart =>
      context.become(inProgress(sender))
      connection ! Message("The Game has started. You can now draw a card.","InGame")
    case JoinQueue =>
      sender ! "You can't do that while waiting for the game to start."
    case Draw =>
      sender ! "The game has not started yet."
  }

  // The game is in progress
  def inProgress(gameActor: ActorRef): Actor.Receive = {
    case Draw =>
      gameActor ! Game.Draw
      context.become(waitingNextTurn)
    case JoinQueue =>
      sender ! "You can't do that while a game is in progress."
  }

  // Waiting for the game actor to end the turn
  def waitingNextTurn: Actor.Receive = {
    case Game.NextTurn(drawnCards) =>
      connection ! drawnCards.map {
        case (name, card) => s"$name has drawn $card"
      }.mkString("\n")
      context.become(inProgress(sender))
    case Game.GameOver =>
      connection ! s"$self lost the game"
      context.stop(self)
    case Game.Victory =>
      connection ! s"$self won the game"
      context.stop(self)
    case JoinQueue =>
      sender ! "You can't do that while a game is in progress."
    case Draw =>
      sender ! "Still waiting for the other player to Draw."
  }
}
