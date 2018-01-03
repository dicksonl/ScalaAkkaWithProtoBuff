package Utilities

import scala.collection.mutable.ListBuffer
import scala.io.Source

/**
  * Created by dickson.lui on 07/11/2017.
  */
object CsvReader {
  def getRowsAsInt(fileName : String) : List[Int] = {
    val fileDir = System.getProperty("user.home") + s"/$fileName.csv"
    val rs = new ListBuffer[Int]()
    for(line <- Source.fromFile(fileDir).getLines){
        rs += line.toInt
    }
    rs.toList
  }
}
