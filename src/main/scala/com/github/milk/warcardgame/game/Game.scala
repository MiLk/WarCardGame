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

          val drawnCards = decks.mapValues(_.head)
          // Remove one card from each deck
          decks = decks.mapValues(_.tail)

          drawnCards.foreach {
            case (player, card) => log.info("{} draws a card {}", player.path.name, card)
          }

          val drawnCardList = drawnCards.values.toList
          // TODO handle draw
          val winnerCard = drawnCardList.maxBy(Deck.score)
          val winnerPlayer = drawnCards.filter(_._2 == winnerCard).head._1

          decks += (winnerPlayer -> (decks(winnerPlayer) ::: util.Random.shuffle(drawnCardList)))

          // Filter the empty decks
          decks = decks.filter(_._2.nonEmpty)

          // Finish if we have no players left
          if (decks.isEmpty) {
            players.foreach(_ ! GameOver)
            context.stop(self)
          }
          // Finish if we have a winner
          else if (decks.size == 1) {
            val winner = decks.head._1
            winner ! Victory
            players.filter(_ != winner).foreach(_ ! GameOver)
            context.stop(self)
          }
          // Next turn if more than 1 player are left
          else {
            decks.keys.foreach(_ ! NextTurn)
            context.become(inProgress(Set.empty[ActorRef]))
          }

        } else context.become(inProgress(newConfirmed))
      }
  }
}
