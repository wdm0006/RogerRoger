package RogerRoger

import oshi.SystemInfo
import org.json4s._
import org.json4s.JsonDSL.WithDouble._
import org.json4s.jackson.JsonMethods._

/** This is our "database" */
object MetricsData {
  private val si = new SystemInfo()
  private val os = si.getOperatingSystem
  private val hal = si.getHardware

  def wrapData(data: JValue, status: JValue): JValue = {
    val response =
      ("time_stamp" -> System.currentTimeMillis / 1000) ~
      ("data" -> data) ~
      ("response" -> status)
    response
  }

  def getElasticSearchHealth(): JValue = {
    val startTime = System.nanoTime()

    try {
      // setup a rest client and issue the request

      // reformat
      val data = ("cluster_health" ->
        ("resp" -> "foo")
      )
      val status =
        ("response" -> 200) ~
          ("took" -> (System.nanoTime() - startTime) * 10E-6)

      wrapData(data, status)
    } catch {
      case err: Throwable => {
        val data = parse(s"""[]""")
        val status =
          ("response" -> 404) ~
            ("took" -> (System.nanoTime() - startTime) * 10E-6) ~
            ("description" -> err.getMessage) ~
            ("stacktrace" -> err.getStackTrace.mkString("\n"))
        wrapData(data, status)
      }

    }

  }

  def getTopHealth(): JValue = {
    val startTime = System.nanoTime()

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
      ("took" -> (System.nanoTime() - startTime) * 10E-6)
    wrapData(data, status)
  }

  def getMissingServiceError(m: String): JValue = {
    val startTime = System.nanoTime()
    val data = parse(s"""[]""")
    val status =
      ("response" -> 404) ~
      ("took" -> (System.nanoTime() - startTime) * 10E-6) ~
      ("description" -> s"no status found for service $m")
    wrapData(data, status)
  }
}