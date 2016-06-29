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

  def getStats(): JValue = {
    val startTime = System.currentTimeMillis

    val data =
        ("top_stats.memory.available" -> hal.getMemory.getAvailable) ~
        ("top_stats.memory.total" -> hal.getMemory.getTotal) ~
        ("top_stats.memory.swap_total" -> hal.getMemory.getSwapTotal) ~
        ("top_stats.memory.swap_used" -> hal.getMemory.getSwapUsed) ~
        ("top_stats.cpu.name" -> hal.getProcessor.getName) ~
        ("top_stats.cpu.physical_processor_count" -> hal.getProcessor.getPhysicalProcessorCount) ~
        ("top_stats.cpu.process_count" -> hal.getProcessor.getProcessCount) ~
        ("top_stats.cpu.logical_processor_count" -> hal.getProcessor.getLogicalProcessorCount) ~
        ("top_stats.cpu.model" -> hal.getProcessor.getModel) ~
        ("top_stats.cpu.thread_count" -> hal.getProcessor.getThreadCount) ~
        ("top_stats.cpu.cpu_load" -> hal.getProcessor.getSystemCpuLoad) ~
        ("top_stats.cpu.system_load_average" -> hal.getProcessor.getSystemLoadAverage) ~
        ("top_stats.cpu.system_io_wait_ticks" -> hal.getProcessor.getSystemIOWaitTicks)


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