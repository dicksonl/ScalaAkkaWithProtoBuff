package Cluster

import Messages.JobMaster.JobMaster.TaskResult
import Messages.JobWorker.JobWorker.Task
import akka.actor.{Actor, ActorLogging, ActorRef, Props, ReceiveTimeout, Terminated}
import scala.concurrent.duration._

object JobWorker {
  trait Event extends Serializable
  def props = Props(new JobWorker)
  case class Work(jobName: String, master: ActorRef) extends Event
  case object WorkLoadDepleted extends Event
}

class JobWorker extends Actor with ActorLogging {
  import JobMaster._
  import JobWorker._
  import context._

  var processed = 0

  def receive = idle

  def idle: Receive = {
    case Work(jobName, master) =>
      become(enlisted(jobName, master))

      log.info(s"Enlisted, will start requesting work for job '${jobName}'.")
      master ! Enlist
      master ! NextTask
      watch(master)

      setReceiveTimeout(30 seconds)
  }

  def enlisted(jobName: String, master: ActorRef): Receive = {
    case ReceiveTimeout =>
      master ! NextTask

    case Task(trips, validVehicles) =>
      processed = processed + 1
      sender ! TaskResult(trips.filter(x => validVehicles.contains(x.vehicleNodeId)))
      sender ! NextTask

    case WorkLoadDepleted =>
      log.info(s"Work load ${jobName} is depleted, retiring...")
      setReceiveTimeout(Duration.Undefined)
      become(retired(jobName))

    case Terminated(master) =>
      setReceiveTimeout(Duration.Undefined)
      log.error(s"Master terminated that ran Job ${jobName}, stopping self.")
      stop(self)
  }

  def retired(jobName: String): Receive = {
    case Terminated(master) =>
      log.error(s"Master terminated that ran Job ${jobName}, stopping self.")
      stop(self)
    case _ => log.error("I'm retired.")
  }
}