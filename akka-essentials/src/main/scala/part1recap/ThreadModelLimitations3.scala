package part1recap

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}

object ThreadModelLimitations3 {

  // DR #3: tracing and dealing with errors is a PITN in multithreaded/distributed apps

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))
  // sum 1M numbers in between 10 threads

  val futures = (0 to 9)
    .map(i => BigInt(100000 * i) until BigInt(100000 * (i + 1))) // 0 - 99999, 100000 - 199999, and so on
    .map(range => Future {
      // bug
      if (range.contains(BigInt(546732))) throw new RuntimeException("invalid number")
      range.sum
    })

  private val sumFuture: Future[BigInt] = Future.reduceLeft(futures)(_ + _)

  def main(args: Array[String]): Unit = {
    sumFuture.onComplete(println)
  }
}
