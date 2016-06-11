package com.github.milk.warcardgame.game


import org.scalatest.{WordSpecLike, MustMatchers}
import org.scalatest.prop.Checkers
import org.scalacheck.Gen.choose
import org.scalacheck.Prop.forAll

class DeckSpec extends Checkers with WordSpecLike with MustMatchers {
  "The score method" must {
    "Return a number between 0 and 15" in {
      check((a: String) => {
        val score = Deck.score(a)
        score >= 0 && score <= 15
      })
    }

    "Return a value between 2 and 15 for cards" in {
      val cardIdx = choose(0, Deck.cardValues.size - 1)
      forAll(cardIdx) {
        idx =>
          val score = Deck.score(Deck.cardValues(idx))
          score >= 2 && score <= 15
      }
    }

    "Return a high score for high cards" in {
      Deck.score("JOKER") == 15 &&
        Deck.score("A") == 14 &&
        Deck.score("K") == 13 &&
        Deck.score("Q") == 12 &&
        Deck.score("J") == 11
    }
  }
}
