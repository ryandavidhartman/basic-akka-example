package part1recap

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}

object ThreadModelLimitations1 extends App {

  // Daniel's rants
  // DR #1: OO encapsulation is only valid in the SINGLE-THREADED MODEL

  class BankAccount(private var amount: Int) {
    override def toString = s"$amount"

    def withdraw(money: Int): Unit = synchronized {
      this.amount -= money
    }

    def deposit(money: Int): Unit = synchronized {
      this.amount += money
    }

    def getAmount: Int = amount
  }

  val account = new BankAccount(2000)
  val depositThreads: Seq[Thread] = (1 to 1000).map(_ => new Thread(() => account.deposit(1)))
  val withdrawThreads: Seq[Thread] = (1 to 1000).map(_ => new Thread(() => account.withdraw(1)))

  def demoRace(): Unit = {
    (depositThreads ++ withdrawThreads).foreach(_.start())
    Thread.sleep(1000)
    println(account.getAmount)
  }

  demoRace()
}



  /*
    - We manually repaired this simple example, with locks (i.e. the synchronized block) but repair to encapsulation
      needed us to fix every method/var in the class

    In more complex examples we still face some issues.  E.g. in a DISTRIBUTED environment how can we in do the locking
    efficiently.

    Problems with manual threading
    - we don't know when the threads are finished
    - race conditions
    - other problems:
       - deadlocks
       - livelocks
   */

