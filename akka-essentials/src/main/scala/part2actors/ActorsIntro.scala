package part2actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorsIntro extends App {

  // part 1 - actor systems
  val actorSystem = ActorSystem("firstActorSystem")  // Note: name must contain only word characters
                                                     // (i.e. [a-zA-Z0-9] plus non-leading '-' or '_')
  println("Actor system: " + actorSystem.name + " started at: " +  actorSystem.startTime)

  // part 2 - create some actors

  /*
    1) Actors are uniquely identified withing an actor
    2) Messages are sent and processes asynchronously
    3) Each actor has maybe respond differently to a given message
    4) Actors are encapsulated so that the only interaction you may have with them is by message passing.
  */

  // An example actor, to count the number of words (in received messages) sent to the actor

  class WordCounter() extends Actor {
    // internal data
    var totalCount = 0

    // behavior
    override def receive: Receive = { // Receive is type PartialFunction[Any, Unit]
      case message: String =>
        println(s"Message received: $message")
        totalCount += message.split(" ").length
      case msg =>
        println(s"[wordCounter] cannot understand message: ${msg.toString}")
    }
  }

  // part 3 - instantiate an actor
  // actor names must be unique for a given actor system
  val wordCounter = actorSystem.actorOf(Props[WordCounter], "wordCounter")
  val anotherCounter = actorSystem.actorOf(Props[WordCounter], "anotherCounter")

  // you also cannot directly instantiate an actor by using its constructor i.e. bob = new WordCounter

  // part 4 - actor communication
  wordCounter ! "I am learning Akka!"
  anotherCounter ! "will I get this message first?"
  // these messages are sent asynchronously and keep be received in a non-deterministic order

  // part 5 how to pass constructor arguments to actors

  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case _ => println(s"implementation doesn't matter, but my name is: $name")
    }
  }

  val person = actorSystem.actorOf(Props(new Person("some name")), "somePerson")  //using the constructor is legal here

  person ! "stupid message"

  // But the best practice is to use a companion object to take the constructor parameters and create a new props object

  object Person {
    def apply(name: String): Props = Props(new Person(name))
  }

  val secondPerson = actorSystem.actorOf(Person("bob"), "someOtherPerson")
  secondPerson ! "hi"

}
