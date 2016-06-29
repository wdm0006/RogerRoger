package RogerRoger.conf

import com.typesafe.config.ConfigFactory

object AppConfig {
  private val config = ConfigFactory.load("rogerroger")

  def getStringOr(key: String, default: String): String = {
    if (config.hasPath(key)) {
      config.getString(key)
    } else {
      default
    }
  }

  def getIntOr(key: String, default: Int): Int = {
    if (config.hasPath(key)) {
      config.getInt(key)
    } else {
      default
    }
  }

  object Server {
    lazy val hostname = getStringOr("app.hostname", "localhost")
    lazy val port = getIntOr("app.port", 5000)
  }

  object Services {
    object ElasticSearch {
      lazy val cluster_name = getStringOr("services.elasticsearch.cluster_name", "elasticsearch")
      lazy val uri = getStringOr("services.elasticsearch.uri", "elasticsearch://localhost:9300")
    }
  }

  object DataStores {
    object ElasticSearch {
      lazy val cluster_name = getStringOr("data_stores.elasticsearch.cluster_name", "elasticsearch")
      lazy val uri = getStringOr("data_stores.elasticsearch.uri", "elasticsearch://localhost:9300")
      lazy val index = getStringOr("data_stores.elasticsearch.index", "rogerroger")
      lazy val mapping = getStringOr("data_stores.elasticsearch.mapping", "metrics")
    }
  }
}