package RogerRoger.services

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods.parse
import org.scalatest.FunSuite

class WrapDataSpec extends FunSuite {
  implicit private val formats = DefaultFormats

  private val services = Seq(
    ("top", TopService.wrapData _),
    ("elasticsearch", ElasticSearchService.wrapData _),
    ("rabbitmq", RabbitMQService.wrapData _)
  )

  services.foreach { case (service, wrapData) =>
    test(s"$service wrapData retains its envelope, status, and object data") {
      val result = wrapData(
        ("metric" -> 42): JObject,
        ("service_response" -> 200) ~ ("took" -> 7)
      )

      assert(result.isInstanceOf[JObject])
      assert((result \ "time_stamp").extract[Long] > 0L)
      assert((result \ "service").extract[String] === service)
      assert((result \ "service_response").extract[Int] === 200)
      assert((result \ "took").extract[Int] === 7)
      assert((result \ "metric").extract[Int] === 42)
    }

    test(s"$service wrapData retains its envelope when data is an empty object") {
      val result = wrapData(parse("{}"), ("service_response" -> 404): JObject)

      assert(result.isInstanceOf[JObject])
      assert((result \ "time_stamp").extract[Long] > 0L)
      assert((result \ "service").extract[String] === service)
      assert((result \ "service_response").extract[Int] === 404)
    }
  }
}
