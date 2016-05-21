package war.client

import akka.actor.{Actor, ActorRef}

case object Connect

case class Connected(lobbyActor: ActorRef)

class Client extends Actor with akka.actor.ActorLogging {

  override def preStart(): Unit = {
    context.parent ! Connect
  }

  def receive = connectionInProgress

  def connectionInProgress: Actor.Receive = {
    case Connected(lobbyActor) =>
      context.become(connected.orElse(waitingForOpponent))
      lobbyActor ! war.lobby.Join
  }

  def connected: Actor.Receive = {
    case war.lobby.Joined => context.become(waitingForOpponent)
  }

  def waitingForOpponent: Actor.Receive = {
    case war.game.GameFound =>
      context.become(waitingForStart)
      sender ! war.game.GameStartConfirmation
  }

  def waitingForStart: Actor.Receive = {
    case war.game.GameStart =>
      context.become(inProgress)
      sender ! war.game.Draw
  }

  def inProgress: Actor.Receive = {
    case war.game.NextTurn =>
      sender ! war.game.Draw
    case war.game.GameOver =>
      log.info("{} lost the game", self)
      context.stop(self)
    case war.game.Victory =>
      log.info("{} won the game", self)
      context.stop(self)
  }
}
