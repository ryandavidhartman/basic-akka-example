package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message: String => println(s"[simple actor] received a string $message")
      case number: Int => println(s"[simple actor] received a number: $number")
      case specialMessage: SpecialMessage => println(s"[simple actor] received a special message : $specialMessage")
    }
  }

  val actorSystem = ActorSystem("actorCapabilitiesDemo")

  val simpleActor = actorSystem.actorOf(Props[SimpleActor], "simpleActor")

  simpleActor ! "Hello, actor"

  // 1 - messages can be of any type
  // But messages should be IMMUTABLE
  // and must be SERIALIZABLE (i.e. JVM can turn it into a byte stream)
  // case classes and case objects are the standard way of doing this
  simpleActor ! 42

  case class SpecialMessage(data: String)

  simpleActor ! SpecialMessage("some data") // goes to a dead letter queue, if we don't handle this type

  // 2 - actors have information about their context and themselves

  class SimpleActorTwo extends Actor {
    // context.self is a reference to this actor.  (like "this" in oop)

    override def receive: Receive = {
      case message: String => println(s"[${context.self}] received a string $message")
      case number: Int => println(s"[${context.self}] received a number: $number")
      case specialMessage: SpecialMessage => println(s"[${context.self}] received a special message : $specialMessage")
      case SendMessagesToYourself(content) => self ! content
    }
  }

  val simpleActorTwo = actorSystem.actorOf(Props[SimpleActorTwo], "simpleActorTwo")
  simpleActorTwo ! "Hi"

  case class SendMessagesToYourself(content: String)
  //you can use the context.self (or just self) to send a message to yourself
  simpleActorTwo ! SendMessagesToYourself("talking to myself")

  // 3 - actors can REPLY to messages using context.sender()

  case class SayHiTo(ref: ActorRef)

  class SimpleActorThree extends Actor {
    override def receive: Receive = {
      case "hi" => context.sender() ! "Hello there!"
      case message: String => println(s"[$self] received a string $message")
      case SayHiTo(ref) => ref ! "hi"
    }
  }

  val alice = actorSystem.actorOf(Props[SimpleActorThree], "alice")
  val bob = actorSystem.actorOf(Props[SimpleActorThree], "bob")

  alice ! SayHiTo(bob)

  // 4 - dead letters
  // What if we do this?
  // alice ! "hi"  => context.sender will be Actor.noSender and we'll get an error replying to a dead letter

  // 5 - forwarding messages
  case class ForwardThisTo(data: String, ref: ActorRef)

  class SimpleActorFour extends Actor {
    override def receive: Receive = {
      case message: String => println(s"[$self] received a string $message from [$sender()]")
      case ForwardThisTo(data, ref) => ref forward data  // forward is a "tell" with the sender set original actor not
        // the actor it came from (i.e. this one)
    }
  }

  val firstActor = actorSystem.actorOf(Props[SimpleActorFour], "first")
  val secondActor = actorSystem.actorOf(Props[SimpleActorFour], "second")

  firstActor ! ForwardThisTo("pass it along", secondActor)

}
