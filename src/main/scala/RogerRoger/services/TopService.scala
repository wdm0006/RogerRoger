package RogerRoger.services

import oshi.SystemInfo
import org.json4s._
import org.json4s.JsonDSL.WithDouble._
import org.json4s.jackson.JsonMethods._

object TopService {
  private val si = new SystemInfo()
  private val os = si.getOperatingSystem
  private val hal = si.getHardware
  implicit val formats = DefaultFormats

  def wrapData(data: JValue, status: JValue): JValue = {
    val response =
      ("time_stamp" -> System.currentTimeMillis / 1000) ~
      ("service" -> "top")
    response merge status merge data
  }

  def getStats: JValue = {
    val startTime = System.currentTimeMillis

    val data =
        ("top_stats_memory_available" -> hal.getMemory.getAvailable) ~
        ("top_stats_memory_total" -> hal.getMemory.getTotal) ~
        ("top_stats_memory_swap_total" -> hal.getMemory.getSwapTotal) ~
        ("top_stats_memory_swap_used" -> hal.getMemory.getSwapUsed) ~
        ("top_stats_cpu_name" -> hal.getProcessor.getName) ~
        ("top_stats_cpu_physical_processor_count" -> hal.getProcessor.getPhysicalProcessorCount) ~
        ("top_stats_cpu_process_count" -> hal.getProcessor.getProcessCount) ~
        ("top_stats_cpu_logical_processor_count" -> hal.getProcessor.getLogicalProcessorCount) ~
        ("top_stats_cpu_model" -> hal.getProcessor.getModel) ~
        ("top_stats_cpu_thread_count" -> hal.getProcessor.getThreadCount) ~
        ("top_stats_cpu_cpu_load" -> hal.getProcessor.getSystemCpuLoad) ~
        ("top_stats_cpu_system_load_average" -> hal.getProcessor.getSystemLoadAverage) ~
        ("top_stats_cpu_system_io_wait_ticks" -> hal.getProcessor.getSystemIOWaitTicks)

    val status =
      ("service_response" -> 200) ~
      ("took" -> (System.currentTimeMillis - startTime))
    wrapData(data, status)
  }

  def getMissingServiceError(m: String): JValue = {
    val startTime = System.currentTimeMillis
    val data = parse("{}")
    val status =
      ("service_response" -> 404) ~
      ("took" -> (System.currentTimeMillis - startTime)) ~
      ("description" -> s"no status found for service $m")
    wrapData(data, status)
  }
}