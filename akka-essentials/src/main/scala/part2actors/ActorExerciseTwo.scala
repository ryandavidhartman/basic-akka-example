package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorExerciseTwo extends App {
  /* Bank Account as a actor
      - Deposit
      - WithDraw
      - Statement

      Replies with
      - Success
      - Failure

      Interacts with another actor
   */

  sealed trait BankAccountCommand
  case class Deposit(amount: Double) extends BankAccountCommand
  case class WithDraw(amount: Double) extends BankAccountCommand
  case object Statement extends BankAccountCommand

  sealed trait BankAccountResponse
  case class Success(msg: String) extends BankAccountResponse
  case class Failure(msg: String) extends BankAccountResponse
  case class StatementBalance(balance: Double) extends BankAccountResponse

  class BankAccount extends Actor {

    private[this] var balance: Double = 0.0
    override def receive: Receive = {
      case Statement => sender() ! StatementBalance(balance)
      case Deposit(amount) =>
        if(amount < 0) {
          sender() ! Failure(s"[$amount] is an valid amount to deposit")
        } else {
          balance += amount
          sender() ! Success(s"[$amount] was deposited")
        }
      case WithDraw(amount) =>
        if (amount > balance) {
          sender() ! Failure(s"[$amount] is an valid amount to withdraw")
        } else {
          balance -= amount
          sender() ! Success(s"[$amount] was withdrawn")
        }
    }
  }

  class BankCustomer(bankAccount: ActorRef) extends Actor {
    override def receive: Receive = {
      case bankAccountCommand: BankAccountCommand => bankAccount ! bankAccountCommand
      case Success(msg) => println(s"[$self] Transaction Success: [$msg]")
      case Failure(msg) => println(s"[$self] Transaction Failure: [$msg]")
      case StatementBalance(balance) => println(s"[$self] account balance is: $balance")
    }
  }

  object BankCustomer {
    def apply(bankAccount: ActorRef): Props = Props(new BankCustomer(bankAccount))
  }

  val actorSystem = ActorSystem("exerciseTwo")
  val bankAccount = actorSystem.actorOf(Props[BankAccount], "bankAccount")
  val bankCustomer = actorSystem.actorOf(BankCustomer(bankAccount), "bankCustomer")

  bankCustomer ! Deposit(200.00)
  bankCustomer ! Statement
  bankCustomer ! Deposit(400.00)
  bankCustomer ! WithDraw(49.00)
  bankCustomer ! Statement

}
