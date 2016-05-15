package war.client

import akka.actor.{Actor, ActorRef}
import war.lobby

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
      lobbyActor ! lobby.Join
  }

  def connected: Actor.Receive = {
    case lobby.Joined => context.become(waitingForOpponent)
    case lobby.OpponentFound(opponent) => log.info("Opponent Found {}", opponent)
  }

  def waitingForOpponent: Actor.Receive = {
    case lobby.OpponentFound(opponent) => log.info("Opponent Found {}", opponent)
  }
}
