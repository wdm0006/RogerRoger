package RogerRoger.data_stores

import org.json4s._
import org.json4s.JsonDSL.WithDouble._
import org.json4s.jackson.JsonMethods._
import com.sksamuel.elastic4s.ElasticDsl._
import org.json4s._
import org.json4s.FieldSerializer._
import org.json4s.jackson.Serialization.write
import com.sksamuel.elastic4s.{ElasticClient,ElasticsearchClientUri}
import com.sksamuel.elastic4s.source.JsonDocumentSource
import org.elasticsearch.common.settings.ImmutableSettings
import RogerRoger.conf.AppConfig


object ElasticSearchStore {
  implicit val formats = DefaultFormats

  def persistDocument(metrics_doc: JValue, request_body: String) = {
    val request = parse(request_body)

    // TODO: check the config info for which datastore to use here
    val data_store = "elasticsearch"

    // some data can be passed through to the doc dumbly
    val pass_thru = (request \ "pass_thru").extract[JValue]

    data_store match {
      case "elasticsearch" => indexDocument(metrics_doc merge pass_thru)
      case _ => println(compact(render(metrics_doc merge pass_thru)))
    }
  }

  def indexDocument(metrics_doc: JValue) = {
    // setup a rest client and issue the request
    val settings = ImmutableSettings.settingsBuilder().put("cluster.name", AppConfig.DataStores.ElasticSearch.cluster_name).build()
    val client = ElasticClient.remote(settings, ElasticsearchClientUri(AppConfig.DataStores.ElasticSearch.uri))

    // setup the mapping and index from config
    val mapping_ = AppConfig.DataStores.ElasticSearch.mapping
    val index_ = AppConfig.DataStores.ElasticSearch.index

    // index the document into our default index/mapping
    val qry = index into index_ -> mapping_ doc JsonDocumentSource(compact(render(metrics_doc)))
    try {
      client.execute{qry}.await
    } catch {
      case err: Throwable => println(err.toString)
    }

  }

  def getTimeSeriesMetric(metric_name: String): JValue = {
    // setup a rest client and issue the request
    val settings = ImmutableSettings.settingsBuilder().put("cluster.name", AppConfig.DataStores.ElasticSearch.cluster_name).build()
    val client = ElasticClient.remote(settings, ElasticsearchClientUri(AppConfig.DataStores.ElasticSearch.uri))

    // get some data
    val limit_val = 1000
    try {
      // setup the mapping and index from config
      val mapping_ = AppConfig.DataStores.ElasticSearch.mapping
      val index_ = AppConfig.DataStores.ElasticSearch.index

      //generate and logout the query
      val qry = search in index_ -> mapping_ rawQuery {s"""{"filtered": {"filter": {"bool": {"must": [{"exists": {"field": "$metric_name"}}]}}}}"""} fields("time_stamp", metric_name) sort (field sort "time_stamp") limit limit_val

      // execute and return the query
      val metrics = client.execute {qry}.await
      val json_metrics = parse(metrics.toString) \ "hits" \ "hits" \ "fields"

      json_metrics
    } catch {
      case err: Throwable => ("error" -> err.toString) ~ ("status" -> 500)
    }
  }
}