package RogerRoger

import oshi.SystemInfo
import org.json4s._
import org.json4s.JsonDSL.WithDouble._
import org.json4s.jackson.JsonMethods._
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.{ElasticClient,ElasticsearchClientUri}
import com.sksamuel.elastic4s.source.{JsonDocumentSource,StringDocumentSource}
import org.elasticsearch.common.settings.ImmutableSettings

object MetricsData {
  private val si = new SystemInfo()
  private val os = si.getOperatingSystem
  private val hal = si.getHardware
  private val cluster_name = "elasticsearch_willmcginnis"
  implicit val formats = DefaultFormats

  def wrapData(data: JValue, status: JValue): JValue = {
    val response =
      ("time_stamp" -> System.currentTimeMillis / 1000) ~
      ("data" -> data) ~
      ("response" -> status)
    response
  }

  def getElasticSearchHealth(): JValue = {
    val startTime = System.currentTimeMillis
    try {
      // setup a rest client and issue the request
      val settings = ImmutableSettings.settingsBuilder().put("cluster.name", cluster_name).build()
      val client = ElasticClient.remote(settings, ElasticsearchClientUri("elasticsearch://localhost:9300"))
      val es_health = client.execute { get cluster health }.await
      val es_stats = client.execute { get cluster stats }.await

      // reformat the stats into a nice blob
      val data = ("cluster_health" -> parse(es_health.toString)) ~ ("cluster_stats" ->  parse(es_stats.toString))
      val status = ("response" -> 200) ~ ("took" -> (System.currentTimeMillis - startTime))

      wrapData(data, status)
    } catch {
      case err: Throwable => {
        val data = parse("[]")
        val status =
          ("response" -> 404) ~
            ("took" -> (System.currentTimeMillis - startTime)) ~
            ("description" -> err.getMessage) ~
            ("stacktrace" -> err.getStackTrace.mkString("\n"))
        wrapData(data, status)
      }
    }
  }

  def getTopHealth(): JValue = {
    val startTime = System.currentTimeMillis
    val disks = hal.getDiskStores.map { ds =>
      ("size" -> ds.getSize) ~
      ("writes" -> ds.getWrites) ~
      ("reads" -> ds.getReads) ~
      ("name" -> ds.getName) ~
      ("serial" -> ds.getSerial) ~
      ("model" -> ds.getModel)
    }

    val networks = hal.getNetworkIFs.map { nw =>
      ("speed" -> nw.getSpeed) ~
      ("name" -> nw.getName)
    }

    val data = List(
      ("memory" ->
        ("available" -> hal.getMemory.getAvailable) ~
        ("total" -> hal.getMemory.getTotal) ~
        ("swap_total" -> hal.getMemory.getSwapTotal) ~
        ("swap_used" -> hal.getMemory.getSwapUsed)
      ) ~
      ("disks" -> disks.toIterable) ~
      ("network" -> networks.toIterable) ~
      ("cpu" ->
        ("name" -> hal.getProcessor.getName) ~
        ("physical_processor_count" -> hal.getProcessor.getPhysicalProcessorCount) ~
        ("process_count" -> hal.getProcessor.getProcessCount) ~
        ("logical_processor_count" -> hal.getProcessor.getLogicalProcessorCount) ~
        ("model" -> hal.getProcessor.getModel) ~
        ("thread_count" -> hal.getProcessor.getThreadCount) ~
        ("cpu_load" -> hal.getProcessor.getSystemCpuLoad) ~
        ("system_load_average" -> hal.getProcessor.getSystemLoadAverage) ~
        ("system_io_wait_ticks" -> hal.getProcessor.getSystemIOWaitTicks)
      ))


    val status =
      ("response" -> 200) ~
      ("took" -> (System.currentTimeMillis - startTime))
    wrapData(data, status)
  }

  def getMissingServiceError(m: String): JValue = {
    val startTime = System.currentTimeMillis
    val data = parse(s"""[]""")
    val status =
      ("response" -> 404) ~
      ("took" -> (System.currentTimeMillis - startTime)) ~
      ("description" -> s"no status found for service $m")
    wrapData(data, status)
  }

  def persistDocument(metrics_doc: JValue, request_body: String) = {
    val request = parse(request_body)
    val data_store = (request \ "data_store").extract[String]
    val connection_information = (request \ "connection_information").extract[JValue]
    val pass_thru = (request \ "pass_thru").extract[JValue]

    data_store match {
      case "elasticsearch" => indexDocument(metrics_doc merge pass_thru)
      case _ => println(compact(render(metrics_doc merge pass_thru)))
    }
  }

  def indexDocument(metrics_doc: JValue) = {
    // TODO: take in the connection information from request here.

    // setup a rest client and issue the request
    val settings = ImmutableSettings.settingsBuilder().put("cluster.name", cluster_name).build()
    val client = ElasticClient.remote(settings, ElasticsearchClientUri("elasticsearch://localhost:9300"))

    client.execute {
      index into "rogerroger/metrics" doc JsonDocumentSource(compact(render(metrics_doc)))
    }

  }
}