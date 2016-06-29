package RogerRoger.services

import org.json4s._
import org.json4s.JsonDSL.WithDouble._
import org.json4s.jackson.JsonMethods._
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.{ElasticClient,ElasticsearchClientUri}
import org.elasticsearch.common.settings.ImmutableSettings
import RogerRoger.conf.AppConfig

object ElasticSearchService {
  implicit val formats = DefaultFormats

  def wrapData(data: JValue, status: JValue): JValue = {
    val response =
      ("time_stamp" -> System.currentTimeMillis / 1000) ~ ("service" -> "elasticsearch")
    response merge status merge data
  }

  def getStats(): JValue = {
    val startTime = System.currentTimeMillis
    try {
      // setup a rest client and issue the request
      val settings = ImmutableSettings.settingsBuilder().put(
        "cluster.name",
        AppConfig.Services.ElasticSearch.cluster_name
      ).build()

      val client = ElasticClient.remote(settings, ElasticsearchClientUri(AppConfig.Services.ElasticSearch.uri))
      val es_health = parse(client.execute { get cluster health }.await.toString)
      val es_stats = parse(client.execute { get cluster stats }.await.toString)

      // reformat the stats into a nice blob

      val data =
        ("elasticsearch_stats.cluster_health.status" -> es_health \ "status") ~
        ("elasticsearch_stats.cluster_stats.indices.docs.count" ->  es_stats \ "indices" \ "docs" \ "count") ~
        ("elasticsearch_stats.cluster_stats.indices.store.size_in_bytes" -> es_stats \ "indices" \ "store" \ "size_in_bytes")

      val status =
        ("service_response" -> 200) ~
        ("took" -> (System.currentTimeMillis - startTime))
      wrapData(data, status)
    } catch {
      case err: Throwable =>
        val data = parse("[]")
        val status =
          ("service_response" -> 404) ~
          ("took" -> (System.currentTimeMillis - startTime)) ~
          ("description" -> err.getMessage) ~
          ("stacktrace" -> err.getStackTrace.mkString("\n"))
        wrapData(data, status)
    }
  }
}