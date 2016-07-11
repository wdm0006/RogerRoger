package RogerRoger.schedulers

import org.json4s._
import org.json4s.jackson.JsonMethods._
import akka.actor._
import scala.concurrent.duration._

class LocalPingActor extends Actor {
  def receive = {
    case _ => {
      implicit lazy val formats = org.json4s.DefaultFormats

      // todo pass in the hostname and port here from config
      val base_url = "http://localhost:5000"
      val options = parse(scala.io.Source.fromURL(base_url + "/stats/options").mkString) \ "services"
      val options_cont = options.extract[Seq[String]]
      options_cont.foreach{x: String =>
        scala.io.Source.fromURL(base_url + x)
        println("ping")
      }
    }
  }
}

object SelfReferencingScheduler {
  // setting up the overall system
  val system = ActorSystem("SelfReferencingSystem")
  val actor = system.actorOf(Props(new LocalPingActor), name = "local_ping_actor")

  def schedule_message(message: String, interval: Int) = {
    import system.dispatcher
    system.scheduler.schedule(30 seconds, interval seconds, actor, message)
  }
}
