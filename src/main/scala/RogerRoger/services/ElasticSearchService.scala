package RogerRoger.services

import org.json4s._
import org.json4s.JsonDSL.WithDouble._
import org.json4s.jackson.JsonMethods._
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.{ElasticClient,ElasticsearchClientUri}
import org.elasticsearch.common.settings.Settings
import org.log4s.getLogger
import RogerRoger.conf.AppConfig

object ElasticSearchService {
  implicit val formats = DefaultFormats

  // one shared transport client for the object lifetime (mirrors ElasticSearchStore)
  private lazy val settings = Settings.settingsBuilder().put(
    "cluster.name",
    AppConfig.Services.ElasticSearch.cluster_name
  ).build()
  private lazy val client = ElasticClient.transport(settings, ElasticsearchClientUri(AppConfig.Services.ElasticSearch.uri))

  def wrapData(data: JValue, status: JValue): JValue = {
    val response =
      ("time_stamp" -> System.currentTimeMillis / 1000) ~ ("service" -> "elasticsearch")
    response merge status merge data
  }

  private val logger = getLogger

  def errorResponse(err: Throwable, startTime: Long): JValue = {
    logger.error(err)("elasticsearch stats collection failed")
    val status =
      ("service_response" -> 404) ~
      ("took" -> (System.currentTimeMillis - startTime)) ~
      ("description" -> "Unable to collect elasticsearch stats")
    wrapData(parse("{}"), status)
  }

  def getStats: JValue = {
    val startTime = System.currentTimeMillis
    try {
      // reuse the shared transport client and issue the request
      val es_health = parse(client.execute { get cluster health }.await.toString)
      val es_stats = parse(client.execute { get cluster stats }.await.toString)

      // reformat the stats into a nice blob

      val data =
        ("elasticsearch_stats_cluster_health_status" -> es_health \ "status") ~
        ("elasticsearch_stats_cluster_stats_indices_docs_count" ->  es_stats \ "indices" \ "docs" \ "count") ~
        ("elasticsearch_stats_cluster_stats_indices_store_size_in_bytes" -> es_stats \ "indices" \ "store" \ "size_in_bytes")

      val status =
        ("service_response" -> 200) ~
        ("took" -> (System.currentTimeMillis - startTime))
      wrapData(data, status)
    } catch {
      case err: Throwable => errorResponse(err, startTime)
    }
  }
}