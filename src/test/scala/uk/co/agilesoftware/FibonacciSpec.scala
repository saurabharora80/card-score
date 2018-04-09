package uk.co.agilesoftware

import org.scalatest.{FlatSpec, Matchers}


class FibonacciSpec extends FlatSpec with Matchers {

  Seq(0 -> 0, 1->1, 2 -> 1, 3 -> 2, 55 -> 139583862445L).foreach { dataItem =>
    s"fibonacci of ${dataItem._1}" should s"be ${dataItem._2}" in {
      Fibonacci(dataItem._1) shouldBe dataItem._2
    }
  }
}

object Fibonacci {
  def apply(n: Int): Long = {
    n match {
      case 0 => 0
      case 1 => 1
      case _ => (2 to n).foldLeft(0L -> 1L) { (x, _) => x._2 -> (x._1 + x._2)}._2
    }

    /*def fibo(n: Int, f1: Long, f2: Long): Long = {
      n match {
        case 0 => 0
        case 1 => f2
        case p => fibo(p-1, f2, f1+f2)
      }
    }

    i match {
      case x => fibo(x, 0, 1)
    }*/
  }
}
