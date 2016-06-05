package com.github.milk.warcardgame.game

import akka.actor.{Props, Actor, ActorRef, ActorSystem}
import akka.testkit.{TestProbe, TestKit}
import org.scalatest.{WordSpecLike, BeforeAndAfterAll, MustMatchers}

object TestGame {
  def props(players: Set[ActorRef]): Props = Props(new Game(players))
}

class TestGame(players: Set[ActorRef]) extends Actor {

  import Game._

  players.foreach(_ ! GameFound)

  def receive = {
    case _ =>
  }
}

class GameSupervisorSpec extends TestKit(ActorSystem("GameSupervisorSpec"))
  with WordSpecLike
  with MustMatchers
  with BeforeAndAfterAll {

  import GameSupervisor._
  import Game._

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A GameSupervisor actor" must {
    "create a new game for the specified players" in {
      val gameSupervisor = system.actorOf(GameSupervisor.props(TestGame.props))
      val lobby = TestProbe()
      val client1 = TestProbe()
      val client2 = TestProbe()
      lobby.send(gameSupervisor, CreateGame(Set(client1.ref, client2.ref)))
      client1.expectMsg(GameFound)
      client2.expectMsg(GameFound)
    }
  }

}
