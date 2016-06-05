package com.github.milk.warcardgame.game

object Deck {
  val cardValues: List[String] = "JOKER" :: List("A", "K", "Q", "J") ::: (2 to 10).reverse.toList.map(_.toString)
  val deckSize = 54

  private def newDeck: List[String] = util.Random.shuffle(cardValues ::: cardValues ::: cardValues.tail ::: cardValues.tail)

  def generatePlayersDecks: (List[String], List[String]) = {
    val deck = newDeck
    (deck.take(deckSize / 2), deck.takeRight(deckSize / 2))
  }

  def score(a: String): Int = a match {
    case "JOKER" => 15
    case "A" => 14
    case "K" => 13
    case "Q" => 12
    case "J" => 11
    case s: String => try {
      s.toInt
    } catch {
      case _: NumberFormatException => 0
    }
    case _ => 0
  }

}
