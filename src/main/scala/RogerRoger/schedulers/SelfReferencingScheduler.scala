package RogerRoger.schedulers

import org.json4s._
import org.json4s.jackson.JsonMethods._
import akka.actor._
import scala.concurrent.duration._

class Supervisor extends Actor {
  import akka.util.Timeout
  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._
  import scala.concurrent.duration._

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: ArithmeticException      => Resume
      case _: NullPointerException     => Restart
      case _: IllegalArgumentException => Restart
      case _: Exception                => Restart
    }

  implicit val timeout = Timeout(10 seconds)

  def receive = {
    case message: String => {
      val pinger = context.actorOf(Props[LocalPingActor], "LocalPingActor")
      pinger ! message
      // todo kill
    }
  }
}

class LocalPingActor extends Actor {
  @throws(classOf[java.io.IOException])
  def get(url: String) = scala.io.Source.fromURL(url)

  def receive = {
    case _ => {
      implicit lazy val formats = org.json4s.DefaultFormats
      // todo pass in the hostname and port here from config
      val base_url = "http://localhost:5000"
      val options = parse(scala.io.Source.fromURL(base_url + "/stats/options").mkString) \ "services"
      val options_cont = options.extract[Seq[String]]
      options_cont.foreach{x: String =>
        try {
          val content = get(base_url + x)
          println("ping")
        } catch {
          case ioe: java.io.IOException =>  println("java io exception")
          case ste: java.net.SocketTimeoutException => println("socket timeout exception")
          case jns: java.net.SocketException => println("socket exception")
          case _: Throwable => println("some kind of issue calling the ping")
        }
      }
    }
  }
}


object SelfReferencingScheduler {
  // setting up the overall system
  val system = ActorSystem("SelfReferencingSystem")
  val supervisor = system.actorOf(Props[Supervisor], "local_ping_supervisor")

  def schedule_message(message: String, interval: Int) = {
    import system.dispatcher
    system.scheduler.schedule(30 seconds, interval seconds, supervisor, message)
  }
}
