package Cluster

import Messages.JobReceptionist.JobReceptionist.JobRequest
import com.typesafe.config.ConfigFactory
import akka.actor.{ActorSystem, Props}
import akka.cluster.Cluster
import Utilities.CsvReader

object Main extends App{
    val conf = ConfigFactory.load
    val system = ActorSystem("distributedTransform", conf)

    println(s"Start node with roles: ${Cluster(system).selfRoles}")

    if(system.settings.config.getStringList("akka.cluster.roles").contains("master")) {
      Cluster(system).registerOnMemberUp {
        println("Master node up.")

        val rcvRegs = CsvReader.getRowsAsInt("RCVsMap")

        val receptionist = system.actorOf(JobReceptionist.props, "receptionist")

        val filteredData = receptionist ! JobRequest(s"Distributed Job", rcvRegs)

        //println(s"Distributed work Completed.")

        system.actorOf(Props(new ClusterDomainEventListener), "cluster-listener")
      }
    }
}
