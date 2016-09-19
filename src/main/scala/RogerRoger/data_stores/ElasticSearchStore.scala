package RogerRoger.data_stores

import org.elasticsearch.search.SearchHit
import org.json4s.JsonDSL.WithDouble._
import org.json4s.jackson.JsonMethods._
import com.sksamuel.elastic4s.ElasticDsl._
import org.json4s._
import com.sksamuel.elastic4s.{ElasticClient,ElasticsearchClientUri}
import com.sksamuel.elastic4s.source.JsonDocumentSource
import org.elasticsearch.common.settings.Settings
import RogerRoger.conf.AppConfig
import org.elasticsearch.indices.IndexAlreadyExistsException
import org.elasticsearch.transport.RemoteTransportException


object ElasticSearchStore {
  implicit val formats = DefaultFormats

  // get the datastore info from on disk config
  private val settings = Settings.settingsBuilder().put("cluster.name", AppConfig.DataStores.ElasticSearch.cluster_name).build()
  private val client = ElasticClient.transport(settings, ElasticsearchClientUri(AppConfig.DataStores.ElasticSearch.uri))
  val mapping_ = AppConfig.DataStores.ElasticSearch.mapping
  val index_ = AppConfig.DataStores.ElasticSearch.index

  // setup the index itself.
  createIndex()

  def createIndex() = {
    try {
      client.execute { create index index_ shards 3 replicas 2 mappings mapping_ }.await
    } catch {
      case _: IndexAlreadyExistsException => // Ok, ignore.
      case _: RemoteTransportException => // Ok, ignore.
    }
  }

  def persistDocument(metrics_doc: JValue, request_body: String) = {
    val request = parse(request_body)
    val pass_thru = (request \ "pass_thru").extract[JValue]
    indexDocument(metrics_doc merge pass_thru)
  }

  def indexDocument(metrics_doc: JValue) = {
    // index the document into our default index/mapping
    val qry = index into index_ -> mapping_ doc JsonDocumentSource(compact(render(metrics_doc)))
    try {
      client.execute{qry}.await
    } catch {
      case err: Throwable => println(err.toString)
    }

  }

  def getTimeSeriesMetric(metric_name: String): JValue = {
    // get some data
    val limit_val = 1000
    try {
      //generate and logout the query
      val qry = search in index_ -> mapping_ rawQuery {s"""{"bool": {"must": [{"exists": {"field": "$metric_name"}}]}}"""} fields("time_stamp", metric_name) sort (field sort "time_stamp") limit limit_val

      // execute and return the query
      val metrics = client.execute {qry}.await
      val json_metrics = parse(metrics.original.toString) \ "hits" \ "hits" \ "fields"

      json_metrics
    } catch {
      case err: Throwable => ("error" -> err.toString) ~ ("status" -> 500)
    }
  }

  def cleanupLog(window: Int) = {
    // deletes all documents older than window seconds from the index
    val limit_ts = (System.currentTimeMillis / 1000) - window

    val ids = client.execute {
      search in index_ -> mapping_ query {
        bool {
          must(
            rangeQuery("time_stamp") to limit_ts
          )
        }
      }
     }.await.getHits.hits()

    // form a container of bulk delete queries
    val bulk_deletes = ids.map {x: SearchHit =>
      delete id x.id from index_ / mapping_
    }
    client.execute { bulk (bulk_deletes) }
  }
}