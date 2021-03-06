package RogerRoger

import java.util.concurrent.Executors
import org.http4s.Request
import org.http4s.dsl._
import RogerRoger.conf.AppConfig

import org.http4s.rho._
import org.http4s.HttpService
import org.http4s.EntityDecoder
import org.http4s.server.staticcontent
import org.http4s.server.staticcontent.ResourceService.Config
import RogerRoger.data_stores._
import RogerRoger.services._

import org.json4s._
import org.json4s.JsonDSL.WithDouble._
import org.json4s.jackson.JsonMethods._

class RhoRoutes extends RhoService {
  // the stats endpoints
  GET / "stats" / 'service |>> {
    (request: Request, service: String) => {
      val body = "{}"
      service match {
        case "elasticsearch" =>
          val data = ElasticSearchService.getStats
          ElasticSearchStore.persistDocument(data, body)
          Ok(pretty(render(data)))
        case "top" =>
          val data = TopService.getStats
          ElasticSearchStore.persistDocument(data, body)
          Ok(pretty(render(data)))
        case "rabbitmq" =>
          val data = RabbitMQService.getStats
          ElasticSearchStore.persistDocument(data, body)
          Ok(pretty(render(data)))
        case "options" => {
          var services_list = AppConfig.Services.services
          services_list = services_list.map {
            x: String => "/stats/".concat(x)
          }
          val data = ("services" -> services_list)
          Ok(pretty(render(data)))
        }
        case _ =>
          val data = TopService.getMissingServiceError(service)
          NotFound(pretty(render(data)))
      }
    }
  }

  // the stats endpoints for post, to index data
  POST / "stats" / 'service ^ EntityDecoder.text |>> {
    (request: Request, service: String, body: String) => {
      service match {
        case "elasticsearch" =>
          val data = ElasticSearchService.getStats
          ElasticSearchStore.persistDocument(data, body)
          Ok(pretty(render(data)))
        case "top" =>
          val data = TopService.getStats
          ElasticSearchStore.persistDocument(data, body)
          Ok(pretty(render(data)))
        case "rabbitmq" =>
          val data = RabbitMQService.getStats
          ElasticSearchStore.persistDocument(data, body)
          Ok(pretty(render(data)))
        case _ =>
          val data = TopService.getMissingServiceError(service)
          NotFound(pretty(render(data)))
      }
    }
  }

  // the metrics endpoint for getting recent raw data
  GET / "metrics" / 'metric_name |>> {
    (request: Request, metric_name: String) => {
      val data = ElasticSearchStore.getTimeSeriesMetric(metric_name)
      Ok(pretty(render(data)))
    }
  }
}

class Routes {
  private implicit val scheduledEC = Executors.newScheduledThreadPool(4)

  // Get the static content
  private val static  = cachedResource(Config("/static", "/static"))
  private val views   = cachedResource(Config("/staticviews", "/"))

  val service: HttpService = HttpService {
    /* Routes for getting static resources. These might be served more efficiently by apache2 or nginx,
     * but its nice to keep the demo self contained */

    // if the route starts with /static, then just serve whatever path it is as a static file
    case r @ GET -> _ if r.pathInfo.startsWith("/static") => {
      static(r)
    }

    // if a directory gets passed, instead of a file, append index.html and try to serve that.
    case r @ GET -> _ if r.pathInfo.endsWith("/") => {
      service(r.withPathInfo(r.pathInfo + "index.html"))
    }

    // if there isn't an extension but it's not a directory, append .html and try to serve it, otherwise just serve
    case r @ GET -> _ =>
      val rr = if (r.pathInfo.contains('.')) r else r.withPathInfo(r.pathInfo + ".html")
      views(rr)
  }

  private def cachedResource(config: Config): HttpService = {
    val cachedConfig = config.copy(cacheStartegy = staticcontent.MemoryCache())
    staticcontent.resourceService(cachedConfig)
  }
}