package actors.client

import play.api.libs.json.{JsString, JsValue}

import scala.collection.Seq

object Connection {
  def generateId : String = java.util.UUID.randomUUID.toString

  case class Message(msg: String, state: String) {
    def toSeq: Seq[(String, JsValue)] = Seq("msg" -> JsString(msg), "state" -> JsString(state))
  }
}

trait Connection {
  import Connection._

  val id = generateId
}
