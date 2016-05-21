package war.game


object Deck {
  val cardValues: List[String] = "JOKER" :: List("A", "K", "Q", "J") ::: (2 to 10).reverse.toList.map(_.toString)
  val deckSize = 54

  private def newDeck: List[String] = util.Random.shuffle(cardValues ::: cardValues ::: cardValues.tail ::: cardValues.tail)

  def generatePlayersDecks: (List[String], List[String]) = {
    val deck = newDeck
    (deck.take(deckSize / 2), deck.takeRight(deckSize / 2))
  }

}
