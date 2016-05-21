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
      context.become(connected)
      lobbyActor ! war.lobby.Join
  }

  private def gameFound(game: ActorRef) = {
    context.become(waitingForStart(game))
    game ! war.game.GameStartConfirmation
  }

  def connected: Actor.Receive = {
    case war.lobby.Joined => context.become(waitingForOpponent)
    case war.game.GameFound => gameFound(sender)
  }

  def waitingForOpponent: Actor.Receive = {
    case war.game.GameFound => gameFound(sender)
  }

  def waitingForStart(game: ActorRef): Actor.Receive = {
    case war.game.GameStart => log.info("Start game {}", sender)
  }
}
