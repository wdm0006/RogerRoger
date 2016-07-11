package RogerRoger.schedulers

import akka.actor._
import scala.concurrent.duration._
import RogerRoger.data_stores._

class LocalCleanupActor extends Actor {
  def receive = {
    case _ => {
      ElasticSearchStore.cleanupLog(3600 * 6)
    }
  }
}

object CleanupScheduler {
  // setting up the overall system
  val system = ActorSystem("CleanupSystem")
  val actor = system.actorOf(Props(new LocalCleanupActor), name = "local_cleanup_actor")

  def schedule_message(message: String, interval: Int) = {
    import system.dispatcher
    system.scheduler.schedule(30 seconds, interval seconds, actor, message)
  }
}
