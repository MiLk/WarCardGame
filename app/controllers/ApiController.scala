package controllers

import javax.inject._

import actors.{lobby, game, client}
import akka.actor.{ActorSystem, ActorRef}
import play.api._
import play.api.mvc._


import scala.concurrent.ExecutionContext

@Singleton
class ApiController @Inject()(system: ActorSystem)(implicit exec: ExecutionContext) extends Controller {
  // Ask pattern
  import akka.pattern.ask
  import scala.concurrent.duration._

  implicit val timeout : akka.util.Timeout = 5.seconds

  // Main actors
  val gameSupervisor = system.actorOf(game.GameSupervisor.props, "gameSupervisor")
  val lobbyActor = system.actorOf(lobby.Lobby.props(gameSupervisor), "lobby")
  val clientSupervisor = system.actorOf(client.ClientSupervisor.props(lobbyActor), "clientSupervisor")

  def connect(id: String) = Action.async {
    (clientSupervisor ? client.ClientSupervisor.Get(id)).mapTo[ActorRef].map { actorRef =>
      Ok(actorRef.path.name)
    }
  }

  def join(id: String) = Action.async {
    for {
      clientActor <- (clientSupervisor ? client.ClientSupervisor.Get(id)).mapTo[ActorRef]
      msg <- (clientActor ? client.Client.JoinQueue).mapTo[String]
    } yield Ok(msg)
  }

  def draw(id: String) = Action.async {
    for {
      clientActor <- (clientSupervisor ? client.ClientSupervisor.Get(id)).mapTo[ActorRef]
      msg <- (clientActor ? client.Client.Draw).mapTo[String]
    } yield Ok(msg)
  }
}
