package Cluster

import Messages.JobMaster.JobMaster.{StartJob, TaskResult}
import Messages.JobReceptionist.JobReceptionist.JobResult
import Messages.JobWorker.JobWorker.Task
import Messages.Trip.Trip
import Utilities.DataStructures
import akka.actor._
import akka.cluster.routing._
import akka.routing._
import scala.collection.mutable
import scala.collection.mutable.{ListBuffer, Queue}
import scala.concurrent.duration._

object JobMaster{
  trait Event extends Serializable
  def props = Props(new JobMaster)
  case object Enlist extends Event
  case object NextTask extends Event
  case object Start extends Event
  case class DisplayResults(result : Int) extends Event
}

class JobMaster extends Actor
                    with ActorLogging
                    with CreateWorkerRouter{
  import JobMaster._
  import JobWorker._
  import context._

  var toProcess: mutable.Queue[List[Trip]] = Queue[List[Trip]]()
  var resultSet: ListBuffer[List[Trip]] = ListBuffer[List[Trip]]()
  var filter: List[Int] = List[Int]()

  var workGiven = 0
  var workReceived = 0
  var workers = Set[ActorRef]()
  val router = createWorkerRouter

  def receive = idle

  def idle : Receive = {
    case StartJob(jobName, dataFilter) =>
      val cancellable = context.system.scheduler.schedule(0 millis, 1000 millis, router, Work(jobName, self))
      context.setReceiveTimeout(60 seconds)

      filter = dataFilter
      toProcess = getData(3)

      become(working(jobName, sender, cancellable))
  }

  def working(jobName: String,
              receptionist: ActorRef,
              cancellable: Cancellable): Receive = {
    case Enlist =>
      watch(sender())
      workers  = workers + sender()

    case NextTask =>
      if(toProcess.nonEmpty){
        sender() ! Task(toProcess.dequeue(), filter)
        workGiven = workGiven + 1
      }

    case TaskResult(result) =>
      workReceived = workReceived + 1
      resultSet += result
      if(workGiven == workReceived) {
        cancellable.cancel()
        become(finishing(jobName, receptionist, workers))
        setReceiveTimeout(Duration.Undefined)
        self ! DisplayResults
      }

    case ReceiveTimeout =>
      if(workers.isEmpty) {
        log.info(s"No workers responded in time. Cancelling job $jobName.")
        stop(self)
      } else setReceiveTimeout(Duration.Undefined)

    case Terminated(worker) =>
      log.info(s"Worker $worker got terminated. Cancelling job $jobName.")
      stop(self)
  }

  def finishing(jobName: String,
                receptionist: ActorRef,
                workers: Set[ActorRef]): Receive = {
    case Terminated(worker) =>
      log.info(s"Job $jobName is finishing. Worker ${worker.path.name} is stopped.")

    case DisplayResults =>
      workers.foreach(stop(_))
      receptionist ! JobResult(jobName, resultSet.flatten.toList)
  }

  def getData(workers: Int): mutable.Queue[List[Trip]] ={
    println("Fetching data from SQL Server")
    val data = Trips.Fetch()
    var slicedJobs = mutable.Queue[List[Trip]]()

    for(item <- DataStructures.cut(data, data.length / 250)){
      slicedJobs += item
    }

    println(s"${data.length} trips divided into ${data.length / 250} jobs")

    slicedJobs
  }
}

trait CreateWorkerRouter { this: Actor =>
  def createWorkerRouter: ActorRef = {
    context.actorOf(
      ClusterRouterPool(RoundRobinPool(10), ClusterRouterPoolSettings(
        totalInstances = 100, maxInstancesPerNode = 20,
        allowLocalRoutees = false, useRoles = Set("worker"))).props(Props[JobWorker]),
      name = "worker-router")
  }
}