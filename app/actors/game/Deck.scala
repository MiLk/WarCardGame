package actors.game

object Deck {
  val cardValues: List[String] = "JOKER" :: List("A", "K", "Q", "J") ::: (2 to 10).reverse.toList.map(_.toString)
  val deckSize = 54

  private def newDeck: List[String] = util.Random.shuffle(cardValues ::: cardValues ::: cardValues.tail ::: cardValues.tail)

  def generatePlayersDecks: (Deck, Deck) = {
    val deck = newDeck
    (new Deck(deck.take(deckSize / 2)), new Deck(deck.takeRight(deckSize / 2)))
  }

  def score(a: String): Int = a match {
    case "JOKER" => 15
    case "A" => 14
    case "K" => 13
    case "Q" => 12
    case "J" => 11
    case _ => try {
      a.toInt
    } catch {
      case _: NumberFormatException => 0
    }
  }

}

class Deck(var cards: List[String]) {
  def draw: String = {
    val drawn = cards.head
    cards = cards.tail
    drawn
  }

  def append(wonCards: List[String]): Unit = {
    cards = cards ::: util.Random.shuffle(wonCards)
  }

  def nonEmpty = cards.nonEmpty
}
