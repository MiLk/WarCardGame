package actors.client

import akka.actor.{ActorRef, Props, FSM}

object Client {
  def props(lobby: ActorRef, connection: ActorRef) = Props(new Client(lobby, connection))

  // events
  sealed trait PlayerEvent
  case object JoinQueue extends PlayerEvent
  case object Draw extends PlayerEvent

  sealed trait GameEvent
  case object QueueJoined extends GameEvent

  // states
  sealed trait State
  case object StandBy extends State
  case object JoiningQueue extends State
  case object InQueue extends State
  case object WaitingForStart extends State
  case object InProgress extends State
  case object WaitingNextTurn extends State

  // data
  sealed trait Data
  case object OutGame extends Data
  case class InGame(gameActor: ActorRef) extends Data

}

class Client(lobby: ActorRef, connection: ActorRef) extends FSM[Client.State, Client.Data] {

  import actors.lobby.Lobby
  import actors.game.Game
  import Client._
  import Connection.{SuccessMessage, ErrorMessage}

  startWith(StandBy, OutGame)

  // The client is not in the queue yet
  when(StandBy) {
    case Event(JoinQueue, _) =>
      lobby ! Lobby.Join
      goto(JoiningQueue)
  }

  // The client has requested to join the queue
  when(JoiningQueue) {
    case Event(QueueJoined, _) =>
      connection ! SuccessMessage("You have joined the waiting queue. Waiting for an opponent.", "InQueue")
      goto(InQueue)
    case Event(JoinQueue, _) =>
      connection ! ErrorMessage("Waiting for confirmation from the lobby.", "JoiningQueue")
      stay
  }

  // The client is in the queue and waiting for an opponent
  when(InQueue) {
    case Event(Game.GameFound, _) =>
      sender ! Game.GameStartConfirmation
      connection ! SuccessMessage("An opponent has been found. Waiting for the game to start.", "WaitingForStart")
      goto(WaitingForStart)
    case Event(JoinQueue, _) =>
      connection ! ErrorMessage("You are already in queue.", "InQueue")
      stay
  }

  // The game has been found and waiting for it to start
  when(WaitingForStart) {
    case Event(Game.GameStart, _) =>
      connection ! SuccessMessage("The Game has started. You can now draw a card.", "InProgress")
      goto(InProgress) using InGame(sender)
  }

  // The game is in progress
  when(InProgress) {
    case Event(Draw, InGame(gameActor)) =>
      gameActor ! Game.Draw
      connection ! SuccessMessage("Waiting for the other player to Draw.", "InProgress")
      goto(WaitingNextTurn)
  }

  // Waiting for the game actor to end the turn
  when(WaitingNextTurn) {
    case Event(Game.NextTurn(drawnCards), _) =>
      connection ! SuccessMessage(drawnCards.map {
        case (name, card) => s"$name has drawn $card"
      }.mkString("\n"), "InProgress")
      goto(InProgress)
    case Event(Game.GameOver, _) =>
      connection ! SuccessMessage("You lost the game.", "GameFinished")
      stop
    case Event(Game.Victory, _) =>
      connection ! SuccessMessage("You won the game.", "GameFinished")
      stop
    case Event(Draw, _) =>
      sender ! ErrorMessage("Still waiting for the other player to Draw.", "WaitingNextTurn")
      stay
  }

  whenUnhandled {
    case Event(Draw, OutGame) =>
      sender ! "You must be in game to draw a card."
      stay
    case Event(JoinQueue, InGame(_)) =>
      sender ! "You can not do that while a game is in progress."
      stay
  }

  onTermination {
    case StopEvent(_, _, _) => context.stop(self)
  }

  initialize()
}
