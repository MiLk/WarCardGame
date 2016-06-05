package com.github.milk.warcardgame.game

import akka.actor.ActorSystem
import akka.testkit.{TestProbe, TestKit}
import org.scalatest.{WordSpecLike, BeforeAndAfterAll, MustMatchers}

class GameSpec extends TestKit(ActorSystem("GameSpec"))
  with WordSpecLike
  with MustMatchers
  with BeforeAndAfterAll {

  import Game._

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A Game actor" must {
    "notify the players when created" in {
      val players = Set(TestProbe(), TestProbe())
      val game = system.actorOf(Game.props(players map (_.ref)))
      players foreach (_ expectMsg GameFound)
    }

    "start the game when both players have confirmed the game" in {
      val players = Set(TestProbe(), TestProbe())
      val game = system.actorOf(Game.props(players map (_.ref)))
      players foreach (_ expectMsg GameFound)
      players foreach (_ send(game, GameStartConfirmation))
      players foreach (_ expectMsg GameStart)
    }

    "must do one turn when both players send the draw message" in {
      val players = Set(TestProbe(), TestProbe())
      val game = system.actorOf(Game.props(players map (_.ref)))
      players foreach (_ expectMsg GameFound)
      players foreach (_ send(game, GameStartConfirmation))
      players foreach (_ expectMsg GameStart)
      players foreach (_ send(game, Draw))
      players foreach (_ expectMsg NextTurn)
    }
  }
}
