package RogerRoger.schedulers

import akka.actor._
import scala.concurrent.duration._
import RogerRoger.data_stores._

class CleanupSupervisor extends Actor {
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
      val pinger = context.actorOf(Props[CleanupActor], "CleanupActor")
      pinger ! message
      pinger ! PoisonPill
    }
  }
}

class CleanupActor extends Actor {
  def receive = {
    case _ => {
      ElasticSearchStore.cleanupLog(3600 * 6)
    }
  }
}

object CleanupScheduler {
  // setting up the overall system
  val system = ActorSystem("CleanupSystem")
  val supervisor = system.actorOf(Props[CleanupSupervisor], "local_cleanup_supervisor")

  def schedule_message(message: String, interval: Int) = {
    import system.dispatcher
    system.scheduler.schedule(30 seconds, interval seconds, supervisor, message)
  }
}
