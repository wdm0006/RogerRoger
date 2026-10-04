package RogerRoger.services

import org.json4s._
import org.scalatest.FunSuite

class ElasticSearchServiceSpec extends FunSuite {
  implicit private val formats = DefaultFormats

  test("getStats keeps its envelope whether the cluster answers or fails") {
    val result = ElasticSearchService.getStats

    assert(result.isInstanceOf[JObject])
    assert((result \ "time_stamp").extract[Long] > 0L)
    assert((result \ "service").extract[String] === "elasticsearch")
    assert((result \ "took").extract[Long] >= 0L)

    val service_response = (result \ "service_response").extract[Int]
    if (service_response === 404) {
      assert((result \ "description") !== JNothing)
      assert((result \ "stacktrace") === JNothing)
    } else {
      assert(service_response === 200)
      assert((result \ "elasticsearch_stats_cluster_health_status") !== JNothing)
    }
  }
}
