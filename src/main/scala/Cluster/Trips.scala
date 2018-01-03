package Cluster

import java.sql._
import Messages.Trip.Trip
import scala.collection.mutable.ListBuffer
import com.microsoft.sqlserver.jdbc._
/**
  * Created by dickson.lui on 07/11/2017.
  */
object Trips {

  def Fetch(): List[Trip] ={
    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver")

    val bespokeConnectionUrl = "jdbc:sqlserver://MAXX-DEV:1433;instanceName=DEMIOS;" +
      "databaseName=Ctrack6_Bespoke;Persist Security Info=True;queryTimeout=600;"

    val con = DriverManager.getConnection(bespokeConnectionUrl, "sa", "%@Pa55w0rd%@")

    val startDate = Helper.GetCurrentSQLDateTimeMinusDays(535)
    val endDate = Helper.GetCurrentSQLDateTimeMinusDays(175)

    val trips = new ListBuffer[Trip]()

    try {
      val cstmt = con.prepareCall("{call dbo.usp_xBDP_GetBigDataCanbusTripsPositions(?,?)}")

      cstmt.setDate(1, startDate)
      cstmt.setDate(2, endDate)

      cstmt.setQueryTimeout(600)

      cstmt.execute()

      var counter = 0l

      val rs = cstmt.getResultSet

      while (rs.next()) {
        var trip = new Trip(
          counter,
          rs.getInt("DummyTripId"),
          rs.getInt("VehicleNodeId"),
          rs.getInt("DriverNodeId"),
          rs.getInt("BusGrpId"),
          Option(new com.google.protobuf.timestamp.Timestamp(rs.getTimestamp("TripStart_UTC").getTime)),
          Option(new com.google.protobuf.timestamp.Timestamp(rs.getTimestamp("TripEnd_UTC").getTime)),
          Option(new com.google.protobuf.timestamp.Timestamp(rs.getTimestamp("TripStart_LT").getTime)),
          Option(new com.google.protobuf.timestamp.Timestamp(rs.getTimestamp("TripEnd_LT").getTime)),
          rs.getInt("DrivingTime"),
          rs.getInt("MaximumSpeed"),
          rs.getInt("OverSpeedCount"),
          rs.getInt("OverSpeedDuration"),
          rs.getInt("SystemOverspeedCount"),
          rs.getInt("SystemOverspeedDuration"),
          rs.getInt("SystemOverspeedDistance"),
          rs.getInt("HarshAccelerationCount"),
          rs.getInt("HarshBrakeCount"),
          rs.getInt("HarshBumpCount"),
          rs.getInt("HarshCorneringCount"),
          rs.getInt("ExcessIdleCount"),
          rs.getInt("ExcessIdleDuration"),
          rs.getInt("FreeWheelDuration"),
          rs.getInt("PanicStatusCount"),
          rs.getInt("OverRevCount"),
          rs.getInt("OverRevDuration"),
          rs.getInt("OverRevDistance"),
          rs.getInt("MaximumRPM"),
          rs.getFloat("TripFuel"),
          rs.getInt("FWSpeed"),
          rs.getInt("FWRPM"),
          rs.getInt("GreenBandTime"),
          rs.getInt("GreenBandLow"),
          rs.getInt("GreenBandHi"),
          rs.getInt("HourMeter"),
          rs.getFloat("StartLongitude"),
          rs.getFloat("StartLatitude"),
          rs.getFloat("StopLongitude"),
          rs.getFloat("StopLatitude"),
          rs.getInt("TripOdo"))

        counter = counter + 1

        trips += trip
      }
    } catch {
      case e => e.printStackTrace
    } finally {
      con.close()
    }

    trips.toList
  }
}

object Helper {
  import java.util.Calendar
  def DayOfWeek(d : Timestamp) : String = {
    val cal = Calendar.getInstance
    cal.setTime(d)
    cal.get(java.util.Calendar.DAY_OF_WEEK).toString
  }

  def GetMPG(tf: Float, distanceInMeters : Int) : String = {
    var result = 0d

    val miles = distanceInMeters * 0.000621371

    val gallons = tf * 0.219969

    result = miles / gallons

    result.toString
  }

  import com.github.nscala_time.time.Imports._
  def GetCurrentSQLDateTimeMinusDays(daysOffset: Int): Date = {
    val utilDate =
      DateTime.now()
        .hour(0)
        .minute(0)
        .second(0)
        .minusDays(daysOffset)
        //        .minusYears(1)
        .toDate
        .getTime

    new java.sql.Date(utilDate)
  }
}