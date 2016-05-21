package war.game

import akka.actor.{ActorRef, Actor, Props}

case object GameFound

case object GameStartConfirmation

case object GameStart

case object Draw

case object NextTurn

case object Victory

case object GameOver

object Game {
  def props(players: Set[ActorRef]): Props = Props(new Game(players))
}

class Game(players: Set[ActorRef]) extends Actor with akka.actor.ActorLogging {

  players.foreach(_ ! GameFound)

  var decks: Map[ActorRef, List[String]] = Map.empty[ActorRef, List[String]]

  def receive = waitingForStart(Set.empty[ActorRef])

  def waitingForStart(confirmed: Set[ActorRef]): Actor.Receive = {
    case GameStartConfirmation =>
      if (!confirmed.contains(sender)) {
        val newConfirmed = confirmed + sender
        if (players.diff(newConfirmed).isEmpty) {

          val deck = Deck.generatePlayersDecks
          decks += (players.head -> deck._1)
          decks += (players.tail.head -> deck._2)
          players.foreach(player => player ! GameStart)
          context.become(inProgress(Set.empty[ActorRef]))

        } else context.become(waitingForStart(newConfirmed))
      }
  }

  def inProgress(confirmed: Set[ActorRef]): Actor.Receive = {
    case Draw =>
      if (!confirmed.contains(sender)) {
        val newConfirmed = confirmed + sender
        if (players.diff(newConfirmed).isEmpty) {

          if (decks.size == 1) {
            val winner = decks.head._1
            winner ! Victory
            players.filter(_ != winner).foreach(_ ! GameOver)
            context.stop(self)
          } else {
            val drawnCards = decks.mapValues(_.head)
            // Remove one card from each deck
            decks = decks.mapValues(_.tail)

            drawnCards.foreach {
              case (player, card) => log.info("{} draws a card {}", player, card)
            }

            // Filter the empty decks
            decks = decks.filter(_._2.nonEmpty)
            // Send next turn to alive players
            if (decks.nonEmpty) {
              decks.keys.foreach(_ ! NextTurn)
              context.become(inProgress(Set.empty[ActorRef]))
            }
            else {
              players.foreach(_ ! GameOver)
              context.stop(self)
            }
          }

        } else context.become(inProgress(newConfirmed))
      }
  }
}
