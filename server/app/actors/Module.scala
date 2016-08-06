package actors

import actors.game.GameSupervisor
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure() = {
    bindActor[game.GameSupervisor]("gameSupervisor", (_) => GameSupervisor.props)
    bindActor[lobby.Lobby]("lobby")
    bindActor[client.ClientSupervisor]("clientSupervisor")
  }

}
