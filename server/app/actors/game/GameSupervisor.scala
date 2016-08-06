package actors.game

import akka.actor.{Actor, ActorRef, Props}

object GameSupervisor {
  def props: Props = Props(new GameSupervisor with GamePropsProvider)

  case class CreateGame(players: Set[ActorRef])

}

trait GamePropsProvider {
  def gameProps: Set[ActorRef] => Props = Game.props
}

class GameSupervisor extends Actor with akka.actor.ActorLogging {
  this: GamePropsProvider =>

  import GameSupervisor._

  def receive = {
    case CreateGame(players) => context.actorOf(gameProps(players))
  }
}
