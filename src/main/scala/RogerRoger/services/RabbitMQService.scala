package RogerRoger.services

import org.json4s._
import org.json4s.JsonDSL.WithDouble._
import org.json4s.jackson.JsonMethods._
import dispatch._
import dispatch.Defaults._
import scala.concurrent.Await
import scala.concurrent.duration._
import org.log4s.getLogger
import RogerRoger.conf.AppConfig

object RabbitMQService {
  implicit val formats = DefaultFormats

  def wrapData(data: JValue, status: JValue): JValue = {
    val response =
      ("time_stamp" -> System.currentTimeMillis / 1000) ~
        ("service" -> "rabbitmq")
    response merge status merge data
  }

  def getVhosts: JValue = {
    val username = AppConfig.Services.RabbitMQ.username
    val password = AppConfig.Services.RabbitMQ.password
    val host = AppConfig.Services.RabbitMQ.host
    val port = AppConfig.Services.RabbitMQ.port
    val page = url(s"http://$username:$password@$host:$port/api/vhosts")
    val request = Http(page.GET)
    val response = Await.result(request, 10 seconds)
    parse(response.getResponseBody)
  }

  private val logger = getLogger

  def errorResponse(err: Throwable, startTime: Long): JValue = {
    logger.error(err)("rabbitmq stats collection failed")
    val status =
      ("service_response" -> 404) ~
      ("took" -> (System.currentTimeMillis - startTime)) ~
      ("description" -> "Unable to collect rabbitmq stats")
    wrapData(parse("{}"), status)
  }

  def getStats: JValue = {
    val startTime = System.currentTimeMillis
    try {
      val data: JValue = ("rabbitmq_stats_vhosts" -> getVhosts)
      val status =
        ("service_response" -> 200) ~
        ("took" -> (System.currentTimeMillis - startTime))
      wrapData(data, status)
    } catch {
      case err: Throwable => errorResponse(err, startTime)
    }
  }
}
