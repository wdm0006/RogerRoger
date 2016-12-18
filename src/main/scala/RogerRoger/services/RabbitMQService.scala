package RogerRoger.services

import org.json4s._
import org.json4s.JsonDSL.WithDouble._
import org.json4s.jackson.JsonMethods._
import dispatch._
import scala.concurrent.Await
import scala.concurrent.duration._

object RabbitMQService {
  private val username: String = "guest"
  private val password: String = "guest"
  private val hostname: String = "localhost"
  private val hostport: String = "localhost"

  def wrapData(data: JValue, status: JValue): JValue = {
    val response =
      ("time_stamp" -> System.currentTimeMillis / 1000) ~
        ("service" -> "top")
    response merge status merge data
  }

  def getVhosts = {
      val page = url(s"http://$username:$password@$hostname:$hostport/api/vhosts")
      val request = Http(page.GET)
      val response = Await.result(request, 10 seconds)
      println("sent request")
  }

  def getStats: JValue = {
    getVhosts
    val foo = ("foo" -> "bar")
    foo
  }
}