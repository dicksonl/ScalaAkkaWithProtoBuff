package Cluster

import java.net.URLEncoder
import Cluster.JobReceptionist.Job
import Messages.JobMaster.JobMaster.StartJob
import Messages.JobReceptionist.JobReceptionist.{JobRequest, JobResult, JobSuccess}
import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, Props, SupervisorStrategy}

object JobReceptionist {
  trait Event extends Serializable
  trait Response extends Event
  def props = Props(new JobReceptionist)
  def name = "receptionist"
  case class Job(name: String, filter: List[Int], respondTo: ActorRef, jobMaster : ActorRef) extends Event
}

class JobReceptionist extends Actor
                          with ActorLogging
                          with CreateMaster{
  import context._

  override def supervisorStrategy: SupervisorStrategy = SupervisorStrategy.stoppingStrategy
  var jobs = Set[Job]()
  var retries = Map[String, Int]()
  val maxRetries = 3

  override def receive: Receive = {
    case JobRequest(name, filter) =>
      log.info(s"Received job $name")
      val masterName = "master-"+URLEncoder.encode(name, "UTF8")
      val jobMaster = createMaster(masterName)
      jobs = jobs + Job(name, filter, sender, jobMaster)
      jobMaster ! StartJob(name, filter)
      watch(jobMaster)

    case JobResult(jobName, result) =>
      jobs.find(_.name == jobName).foreach { job =>
        job.respondTo ! JobSuccess(jobName, result)
        stop(job.jobMaster)
        jobs = jobs - job
        log.info(s"Job Completed, retiring...")
        CsvWriter.WriteToFile(result)
      }
  }
}

trait CreateMaster {
  def context: ActorContext
  def createMaster(name: String) = context.actorOf(JobMaster.props, name)
}