package com.github.milk.warcardgame.lobby

import akka.actor.{Props, Actor, ActorSystem}
import akka.testkit.{TestProbe, TestKit}
import org.scalatest.{WordSpecLike, BeforeAndAfterAll, MustMatchers}


class LobbySpec extends TestKit(ActorSystem("LobbySpec"))
  with WordSpecLike
  with MustMatchers
  with BeforeAndAfterAll {

  import Lobby._
  import com.github.milk.warcardgame.game.GameSupervisor._

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A Lobby actor" must {
    "put a joining client in queue" in {
      val client = TestProbe()
      val gameSupervisor = TestProbe()
      val lobby = system.actorOf(Lobby.props(gameSupervisor.ref))
      client.send(lobby, Join)
      client.expectMsg(Joined)
      gameSupervisor.expectNoMsg()
    }

    "ignore a client joining more than once" in {
      val client = TestProbe()
      val gameSupervisor = TestProbe()
      val lobby = system.actorOf(Lobby.props(gameSupervisor.ref))
      client.send(lobby, Join)
      client.expectMsg(Joined)
      client.send(lobby, Join)
      client.send(lobby, Join)
      client.expectNoMsg()
      gameSupervisor.expectNoMsg()
    }

    "create a game if 2 different clients are in the queue" in {
      val client1 = TestProbe()
      val client2 = TestProbe()
      val gameSupervisor = TestProbe()
      val lobby = system.actorOf(Lobby.props(gameSupervisor.ref))
      client1.send(lobby, Join)
      client2.send(lobby, Join)
      gameSupervisor.expectMsgPF() {
        case CreateGame(set)
          if set.size == 2 &&
            set.contains(client1.ref) &&
            set.contains(client2.ref) => ()
      }
    }

    "a client should be able to leave the queue" in {
      val client = TestProbe()
      val gameSupervisor = TestProbe()
      val lobby = system.actorOf(Lobby.props(gameSupervisor.ref))
      client.send(lobby, Join)
      client.expectMsg(Joined)
      client.send(lobby, Leave)
      client.expectMsg(Left)
      gameSupervisor.expectNoMsg()
    }

    "a client should be told if not in the queue" in {
      val client = TestProbe()
      val gameSupervisor = TestProbe()
      val lobby = system.actorOf(Lobby.props(gameSupervisor.ref))
      client.send(lobby, Join)
      client.expectMsg(Joined)
      client.send(lobby, Leave)
      client.expectMsg(Left)
      client.send(lobby, Leave)
      client.expectMsg(NotInQueue)
      gameSupervisor.expectNoMsg()
    }

    "a client should no longer be in the queue if a game has been found" in {
      val client1 = TestProbe()
      val client2 = TestProbe()
      val gameSupervisor = TestProbe()
      val lobby = system.actorOf(Lobby.props(gameSupervisor.ref))
      client1.send(lobby, Join)
      client1.expectMsg(Joined)
      client2.send(lobby, Join)
      gameSupervisor.expectMsgPF() {
        case CreateGame(_) => ()
      }
      client1.send(lobby, Leave)
      client1.expectMsg(NotInQueue)
      client2.send(lobby, Leave)
      client2.expectMsg(NotInQueue)
    }
  }

}
