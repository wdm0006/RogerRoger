package RogerRoger

import java.util.concurrent.Executors

import com.typesafe.config.ConfigFactory
import org.http4s.rho.swagger.SwaggerSupport
import org.http4s.{ Service, HttpService }
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.ServerBuilder
import org.log4s.getLogger
import RogerRoger.conf.AppConfig
import RogerRoger.schedulers.{SelfReferencingScheduler,CleanupScheduler}


class RogerRogerApp(host: String, port: Int) {
  private val logger = getLogger
  private val pool = Executors.newCachedThreadPool()

  logger.info(s"Starting Http4s-blaze example on '$host:$port'")

  // our routes can be combinations of any HttpService, regardless of where they come from
  def rhoRoutes = new RhoRoutes().toService(SwaggerSupport())

  // our routes can be combinations of any HttpService, regardless of where they come from
  val routes = Service.withFallback(rhoRoutes)(new Routes().service)

  // Add some logging to the service
  val service: HttpService = routes.local{ req =>
    val path = req.uri.path
    logger.info(s"${req.remoteAddr.getOrElse("null")} -> ${req.method}: $path")
    req
  }

  // Construct the blaze pipeline.
  def build(): ServerBuilder =
    BlazeBuilder
      .bindHttp(port, host)
      .mountService(service)
      .withServiceExecutor(pool)
}

object RogerRogerApp {
  // start up the self-referencing scheduler
  val self_ping_scheduler = SelfReferencingScheduler
  self_ping_scheduler.schedule_message("local", 30)

  // start up the cleanup scheduler
  val cleanup_scheduler = CleanupScheduler
  cleanup_scheduler.schedule_message("nil", 300)

  // start up the app itself
  val conf = ConfigFactory.load("rogerroger")
  def main(args: Array[String]): Unit = {
    new RogerRogerApp(AppConfig.Server.hostname, AppConfig.Server.port)
      .build()
      .run
      .awaitShutdown()
  }
}