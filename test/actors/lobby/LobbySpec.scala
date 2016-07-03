package actors.lobby

import akka.actor.{ActorRef, Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestProbe, TestKit}
import org.scalatest.{WordSpecLike, BeforeAndAfterAll, MustMatchers}

object TestLobby {
  def props(gameSupervisor: ActorRef): Props = Props(new TestLobby(gameSupervisor))

  case object GetWaitingQueue

  case class WaitingQueue(wq: List[ActorRef])

}

class TestLobby(gameSupervisor: ActorRef) extends Lobby(gameSupervisor) {

  import TestLobby._

  override def receive = super.receive orElse {
    case GetWaitingQueue => sender ! WaitingQueue(waitingQueue.toList)
  }
}

class LobbySpec extends TestKit(ActorSystem("LobbySpec"))
  with ImplicitSender
  with WordSpecLike
  with MustMatchers
  with BeforeAndAfterAll {

  import Lobby._
  import TestLobby._
  import actors.game.GameSupervisor._
  import actors.client.Client._

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A Lobby actor" must {
    "put a joining client in queue" in {
      val client = TestProbe()
      val gameSupervisor = TestProbe()
      val lobby = system.actorOf(Lobby.props(gameSupervisor.ref))
      client.send(lobby, Join)
      client.expectMsg(QueueJoined)
      gameSupervisor.expectNoMsg()
    }

    "ignore a client joining more than once" in {
      val client = TestProbe()
      val gameSupervisor = TestProbe()
      val lobby = system.actorOf(Lobby.props(gameSupervisor.ref))
      client.send(lobby, Join)
      client.expectMsg(QueueJoined)
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
      client.expectMsg(QueueJoined)
      client.send(lobby, Leave)
      client.expectMsg(Left)
      gameSupervisor.expectNoMsg()
    }

    "a client should be told if not in the queue" in {
      val client = TestProbe()
      val gameSupervisor = TestProbe()
      val lobby = system.actorOf(Lobby.props(gameSupervisor.ref))
      client.send(lobby, Join)
      client.expectMsg(QueueJoined)
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
      client1.expectMsg(QueueJoined)
      client2.send(lobby, Join)
      client2.expectMsg(QueueJoined)
      gameSupervisor.expectMsgPF() {
        case CreateGame(_) => ()
      }
      client1.send(lobby, Leave)
      client1.expectMsg(NotInQueue)
      client2.send(lobby, Leave)
      client2.expectMsg(NotInQueue)
    }
  }

  "A TestLobby Actor" must {
    "put a client in the queue on Join message" in {
      val client = TestProbe()
      val gameSupervisor = TestProbe()
      val lobby = system.actorOf(TestLobby.props(gameSupervisor.ref))
      client.send(lobby, Join)
      client.expectMsg(QueueJoined)
      gameSupervisor.expectNoMsg()
      lobby ! GetWaitingQueue
      expectMsg(WaitingQueue(List(client.ref)))
      client.send(lobby, Join)
      client.expectNoMsg()
      lobby ! GetWaitingQueue
      expectMsg(WaitingQueue(List(client.ref)))
    }

    "remove a client from the queue when a game is found" in {
      val client1 = TestProbe()
      val client2 = TestProbe()
      val gameSupervisor = TestProbe()
      val lobby = system.actorOf(TestLobby.props(gameSupervisor.ref))
      client1.send(lobby, Join)
      client2.send(lobby, Join)
      lobby ! GetWaitingQueue
      expectMsg(WaitingQueue(List.empty[ActorRef]))
    }

    "remove the client from the queue on Leave message" in {
      val client = TestProbe()
      val gameSupervisor = TestProbe()
      val lobby = system.actorOf(TestLobby.props(gameSupervisor.ref))
      client.send(lobby, Join)
      client.expectMsg(QueueJoined)
      gameSupervisor.expectNoMsg()
      lobby ! GetWaitingQueue
      expectMsg(WaitingQueue(List(client.ref)))
      client.send(lobby, Leave)
      client.expectMsg(Left)
      gameSupervisor.expectNoMsg()
      lobby ! GetWaitingQueue
      expectMsg(WaitingQueue(List.empty[ActorRef]))
    }
  }

}
