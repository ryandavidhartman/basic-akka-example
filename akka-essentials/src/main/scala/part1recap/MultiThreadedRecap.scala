package part1recap

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object MultiThreadedRecap extends App {

  // creating a new thread on the JVM
  val aThread = new Thread(new Runnable {
    override def run(): Unit = println("I'm running in parallel")
  })

  // with some Scala sugar
  val aThreadSugared = new Thread(() => println("I'm running in parallel"))

  // Running a thread
  aThreadSugared.start()  //Remember calling run directly isn't what we whant
  aThreadSugared.join()

  // Thread scheduling is non-deterministic

  val helloThread = new Thread(() => (1 to 10).foreach(i => println(s"Hello $i")))
  val goodByeThread = new Thread(() => (1 to 10).foreach(i => println(s"Good Bye $i")))

  helloThread.start()
  goodByeThread.start()
  // There is no way to predict the order that things will be printed

  class BankAccount1(private var amount: Int) {
    override def toString(): String = amount.toString
    //This is totally not thread safe
    def withDraw(money: Int) = this.amount -= money
  }

  /* Example of mutli-threading problems
     BA(10,0000)

     Thread1 -> withdraw 1000
     Thread2 -> withdraw 2000

     Execution Order
     Thread1 -> this.amount = this.amount - {{{ PREEMPTED BY OS TO RUN THREAD 2 }}}
     Thread2 -> this.amount = this.amount - 2000, now amount is 8000
     Thread1 -> runs again  this.amount = 10,000(the original value!!!) - 1000 = 9,0000
     whomp whomp wrong result
  */

  // Classic Java fix is to use a synchronize block
  class BankAccount2(private var amount: Int) {  //possibly you could use a @volatile to make it read
    //thread safe
    override def toString(): String = amount.toString

    // use a synchronized block to make it thread safe
    def withDraw(money: Int) = this.synchronized {
      this.amount -= money
    }
  }

  // inter-thread communication on the JVM is handled by the wait - notify mechanism
  // https://www.baeldung.com/java-wait-notify

  // Scala Futures

  val future = Future {
    // This runs on a a different thread
    42
  }

  // we can do call backs on futures

  future.onComplete {
    case Success(v) => println(s"The meaning of life is $v")
    case Failure(_) => print("Not able to calculate the meaning of life")
  }

  // Futures have all the monadic methods (map, flatMap and filters)

  val aProcessedFuture = future.map(_ + 1) // Future with 43
  val aFlatFuture = future.flatMap { value =>
    Future(value + 2)
  }  // Future with 44

  val aFilteredFuture = future.filter(_ % 2 != 0)

  aFilteredFuture.onComplete {
    case Success(v) => println(s"The meaning of life is $v")
    case Failure(e) => print(e.getMessage) //Future.filter predicate is not satisfied
  }

  // We can of course also using for comprehensions
  val junk = for {
    meaningOfLife <- future
    filteredMeaning <- aFilteredFuture
  } yield meaningOfLife + filteredMeaning

  // Other topics on Futures
  // andThen, recover and recoverWith

  // Writeable future from Promises

}

