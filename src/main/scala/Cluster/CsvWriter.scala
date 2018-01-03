package Cluster

import java.sql.Timestamp
import java.text.SimpleDateFormat
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

object CsvWriter {
  import Messages.Trip.Trip

  def WriteToFile(trips: List[Trip]) = {
    import au.com.bytecode.opencsv.CSVWriter
    import java.io.FileWriter

    val writer = new CSVWriter(new FileWriter("data.csv"), ',')
    val header = scala.Array(
      "CassandraTripId",
      "DummyTripId",
      "VehicleNodeId",
      "BusGrpId",
      "TripStart_UTC",
      "TripEnd_UTC",
      "TripStart_LT",
      "TripEnd_LT",
      "DrivingTime",
      "MaximumSpeed",
      "OverSpeedCount",
      "OverSpeedDuration",
      "SystemOverspeedCount",
      //    "SystemOverspeedDuration",
      //    "SystemOverspeedDistance",
      "HarshAccelerationCount",
      "HarshBrakeCount",
      "HarshBumpCount",
      "HarshCorneringCount",
      "ExcessIdleCount",
      "ExcessIdleDuration",
      "FreeWheelDuration",
      //    "PanicStatusCount",
      "OverRevCount",
      "OverRevDuration",
      "OverRevDistance",
      "MaximumRpm",
      "TripFuel",
      "Distance",
      "MPG",
      "DayOfWeek",
      "FWSpeed",
      "FWRPM",
      "GreenBandTime",
      "GreenBandLow",
      "GreenBandHi",
      "HourMeter",
      "StartLongitude",
      "StartLatitude",
      "StopLongitude",
      "StopLatitude"
    )

    val allStrings = new ListBuffer[scala.Array[String]]()

    allStrings += header

    for(n <- trips.indices){
      if(trips(n).tripFuel > 0.01f) { //&& rcvs.contains(trips(n).VehicleNodeId)
        allStrings += scala.Array(
          trips(n).cassandraTripId.toString,
          trips(n).dummyTripId.toString,
          trips(n).vehicleNodeId.toString,
          trips(n).busGrpId.toString,
          new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Timestamp(trips(n).tripStartUTC.get.seconds)),
          new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Timestamp(trips(n).tripEndUTC.get.seconds)),
          new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Timestamp(trips(n).tripStartLT.get.seconds)),
          new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Timestamp(trips(n).tripEndLT.get.seconds)),
          trips(n).drivingTime.toString,
          trips(n).maximumSpeed.toString,
          trips(n).overSpeedCount.toString,
          trips(n).overSpeedDuration.toString,
          trips(n).systemOverSpeedCount.toString,
          trips(n).harshAccelerationCount.toString,
          trips(n).harshBrakeCount.toString,
          trips(n).harshBumpCount.toString,
          trips(n).harshCorneringCount.toString,
          trips(n).excessIdleCount.toString,
          trips(n).excessIdleDuration.toString,
          trips(n).freeWheelDuration.toString,
          trips(n).overRevCount.toString,
          trips(n).overRevDuration.toString,
          trips(n).overRevDistance.toString,
          trips(n).maximumRpm.toString,
          trips(n).tripFuel.toString,
          trips(n).tripOdo.toString,
          Helper.GetMPG(trips(n).tripFuel, trips(n).tripOdo),
          Helper.DayOfWeek(new Timestamp(trips(n).tripStartLT.get.seconds)),
          trips(n).fWSpeed.toString,
          trips(n).fWRPM.toString,
          trips(n).greenBandTime.toString,
          trips(n).greenBandLow.toString,
          trips(n).greenBandHi.toString,
          trips(n).hourMeter.toString,
          trips(n).startLongitude.toString,
          trips(n).startLatitude.toString,
          trips(n).stopLongitude.toString,
          trips(n).stopLatitude.toString
        )
      }
    }

    writer.writeAll(allStrings.asJava)

    writer.close()
  }


}
