package war.client

import akka.actor.{Actor, ActorRef}

object Client {
  case object Connect
  case class Connected(lobbyActor: ActorRef)
}

class Client extends Actor with akka.actor.ActorLogging {
  import Client._
  import war.lobby.Lobby._
  import war.game.Game._

  override def preStart(): Unit = {
    context.parent ! Connect
  }

  def receive = connectionInProgress

  // Communication with Lobby actor
  def connectionInProgress: Actor.Receive = {
    case Connected(lobbyActor) =>
      context.become(connected.orElse(waitingForOpponent))
      lobbyActor ! Join
  }

  def connected: Actor.Receive = {
    case Joined => context.become(waitingForOpponent)
  }

  // Communication with Game actor
  def waitingForOpponent: Actor.Receive = {
    case GameFound =>
      context.become(waitingForStart)
      sender ! GameStartConfirmation
  }

  def waitingForStart: Actor.Receive = {
    case GameStart =>
      context.become(inProgress)
      sender ! Draw
  }

  def inProgress: Actor.Receive = {
    case NextTurn =>
      sender ! Draw
    case GameOver =>
      log.info("{} lost the game", self)
      context.stop(self)
    case Victory =>
      log.info("{} won the game", self)
      context.stop(self)
  }
}
