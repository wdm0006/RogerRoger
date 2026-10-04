package RogerRoger.services

import org.json4s._
import org.scalatest.FunSuite

class ErrorResponseSpec extends FunSuite {
  implicit private val formats = DefaultFormats

  private val secret = "guest:s3cret@http://rabbit.internal:15672/api/vhosts"

  private val cases: Seq[(String, (Throwable, Long) => JValue)] = Seq(
    "elasticsearch" -> ElasticSearchService.errorResponse _,
    "rabbitmq" -> RabbitMQService.errorResponse _
  )

  cases.foreach { case (service, errorResponse) =>
    test(s"$service errorResponse keeps the envelope and hides exception details") {
      val start = System.currentTimeMillis - 5
      val result = errorResponse(new RuntimeException(secret), start)

      assert(result.isInstanceOf[JObject])
      assert((result \ "time_stamp").extract[Long] > 0L)
      assert((result \ "service").extract[String] === service)
      assert((result \ "service_response").extract[Int] === 404)
      assert((result \ "took").extract[Long] >= 5L)
      assert((result \ "description").extract[String] === s"Unable to collect $service stats")
      assert((result \ "stacktrace") === JNothing)

      val body = org.json4s.jackson.JsonMethods.compact(org.json4s.jackson.JsonMethods.render(result))
      assert(!body.contains("s3cret"))
      assert(!body.contains("rabbit.internal"))
      assert(!body.contains("RuntimeException"))
    }
  }
}
