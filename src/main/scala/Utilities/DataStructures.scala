package Utilities

/**
  * Created by dickson.lui on 07/11/2017.
  */
object DataStructures {
  def cut[Trip](xs: List[Trip], n: Int) = {
    val (quot, rem) = (xs.size / n, xs.size % n)
    val (smaller, bigger) = xs.splitAt(xs.size - rem * (quot + 1))
    smaller.grouped(quot) ++ bigger.grouped(quot + 1)
  }
}
