package RogerRoger.services

import org.json4s._
import org.json4s.JsonDSL.WithDouble._
import org.json4s.jackson.JsonMethods._

object RabbitMQService {
  // TODO: all

  def wrapData(data: JValue, status: JValue): JValue = {
    val response =
      ("time_stamp" -> System.currentTimeMillis / 1000) ~
        ("service" -> "top")
    response merge status merge data
  }

  def getStats: JValue = {
    val foo = ("foo" -> "bar")
    foo
  }
}