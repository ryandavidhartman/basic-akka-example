package part2actors

import akka.actor.{Actor, ActorSystem, Props}

/**
 * Create a Counter actor that responds to the following messages:
 * Increment - increment the internal counter variable by 1
 * Decrement - decrement the internal counter variable by 1
 * Print     - print the current counter state
 */
object ActorExerciseOne extends App {

  // Messages
  sealed trait Command
  case object Increment extends Command
  case object Decrement extends Command
  case object Print extends Command

  class Counter extends Actor {
    var count: Int = 0

    override def receive: Receive = {
      case Increment => count += 1
      case Decrement => count -= 1
      case Print => println(s"[$self] has counter value of: $count")
    }
  }

  val actorSystem = ActorSystem("exerciseOne")

  val counter1 = actorSystem.actorOf(Props[Counter], "counterOne")

  counter1 ! Increment
  counter1 ! Increment
  counter1 ! Increment
  counter1 ! Decrement
  counter1 ! Print

}
