package war.game

import akka.actor.{Actor, ActorRef}

case class CreateGame(players: Set[ActorRef])

class GameSupervisor extends Actor with akka.actor.ActorLogging {
  def receive = {
    case CreateGame(players) => context.actorOf(Game.props(players))
  }
}
