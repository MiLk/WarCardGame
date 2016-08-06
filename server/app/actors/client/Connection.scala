package actors.client

import play.api.libs.json.{JsString, JsValue}

import scala.collection.Seq

object Connection {
  def generateId: String = java.util.UUID.randomUUID.toString

  abstract class Message(msg: String, state: String) {
    def toSeq: Seq[(String, JsValue)] = Seq("message" -> JsString(msg), "state" -> JsString(state))
  }
  case class SuccessMessage(msg: String, state: String) extends Message(msg, state)
  case class ErrorMessage(msg: String, state: String) extends Message(msg, state)

}

trait Connection {

  import Connection._

  val id = generateId
}
