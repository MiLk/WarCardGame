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
      context.become(waitingForStart(sender))
      sender ! war.game.GameStartConfirmation
  }

  def waitingForStart(game: ActorRef): Actor.Receive = {
    case war.game.GameStart => log.info("Start game {}", sender)
  }
}
