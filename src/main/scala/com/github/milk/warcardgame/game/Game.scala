package com.github.milk.warcardgame.game

import akka.actor.{ActorRef, Actor, Props}

object Game {
  def props(players: Set[ActorRef]): Props = Props(new Game(players))

  // Queries
  case object GameStartConfirmation

  case object Draw

  // Notifications
  case object GameFound

  case object GameStart

  case object NextTurn

  case object Victory

  case object GameOver

}

class Game(players: Set[ActorRef]) extends Actor with akka.actor.ActorLogging {

  import Game._

  players.foreach(_ ! GameFound)

  def waitingForConfirmation(confirmed: Set[ActorRef], state: Set[ActorRef] => Actor.Receive)(block: => Unit): Unit = {
    if (!confirmed.contains(sender)) {
      val newConfirmed = confirmed + sender
      if (players.diff(newConfirmed).isEmpty) {
        block
      } else context.become(state(newConfirmed))
    }
  }

  def receive = waitingForStart(Set.empty[ActorRef])

  def waitingForStart(confirmed: Set[ActorRef]): Actor.Receive = {
    case GameStartConfirmation =>
      waitingForConfirmation(confirmed, waitingForStart) {
        val deck = Deck.generatePlayersDecks
        val decks = Map(players.head -> deck._1, players.tail.head -> deck._2)
        players.foreach(player => player ! GameStart)
        context.become(inProgress(decks)(Set.empty[ActorRef]))
      }
  }

  def inProgress(decks: Map[ActorRef, Deck])(confirmed: Set[ActorRef]): Actor.Receive = {
    case Draw =>
      waitingForConfirmation(confirmed, inProgress(decks)) {

        val drawnCards = decks.mapValues(_.draw).toList

        drawnCards.foreach {
          case (player, card) => log.info("{} draws a card {}", player.path.name, card)
        }

        val drawnCardList = drawnCards.map(_._2)
        // TODO handle draw
        val winnerCard = drawnCardList.maxBy(Deck.score)
        val winnerPlayer = drawnCards.filter(_._2 == winnerCard).head._1

        decks(winnerPlayer) append drawnCardList

        // Get the players left by filtering the empty decks
        val playersLeft = decks.filter(_._2.nonEmpty).keys

        // Finish if we have no players left
        if (playersLeft.isEmpty) {
          players.foreach(_ ! GameOver)
          context.stop(self)
        }
        // Finish if we have a winner
        else if (playersLeft.size == 1) {
          val winner = playersLeft.head
          winner ! Victory
          players.filter(_ != winner).foreach(_ ! GameOver)
          context.stop(self)
        }
        // Next turn if more than 1 player are left
        else {
          playersLeft.foreach(_ ! NextTurn)
          context.become(inProgress(decks)(Set.empty[ActorRef]))
        }
      }
  }
}
