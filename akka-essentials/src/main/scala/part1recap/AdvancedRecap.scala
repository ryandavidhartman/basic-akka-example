package part1recap

import scala.util.{Failure, Success, Try}

object AdvancedRecap extends App {

  // partial functions
  // i.e. functions that can only be applied to a partial range of the input domain

  val pf1 = (x: Int) => x match {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }

  assert(pf1(1) == 42)

  val try1 = Try { pf1(6)}

  try1 match {
    case Success(value) => println(s"pf1(6) = $value")
    case Failure(exception) =>  println(s"pf1(6) throws ${exception.getLocalizedMessage}")
  }

  // syntax for a partial function!
  val pf2: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }

  assert(pf1(1) == pf2(1))

  // Partial Functions are subtypes of Functions
  // i.e. we can use a PF where ever we use a regular function

  val function: (Int => Int) = pf1

  val modifiedList = Try { List(1,2,3).map(pf2) }

  // Define a PF on the fly

  val modifiedList2 = List(1,2,3).map {
    case 1 => 42
    case _ => 0
  }

  // Lifting Partial Functions
  val lifted = pf2.lift  // (Int => Option[Int])
  assert(lifted(1) == Some(42))
  assert(lifted(100) == None)

  // Chaining Partial Functions
  val pfChain = pf2.orElse[Int, Int] {
    case 99 => 101
  }
  assert(pfChain(1) == 42)
  assert(pfChain.lift(100) == None)
  assert(pfChain(99) == 101)


  // Type aliases
  type ReceiveFunction = PartialFunction[Any, Unit]

  def receive: ReceiveFunction = {
    case 1 => println("Hello")
    case _ => println("Confused...")
  }

  // Implicit vals
  implicit val timeout = 3000
  def callWithTimeout(f: () => Unit)(implicit timeout: Int) = f()
  callWithTimeout(() => println("hi"))  //works because we have an implicit int in scope!

  // Implicit conversions

  // Implicit defs
  // Given:
  case class Person(name: String) {
    def greet = println(s"Hi, my name is $name")
  }

  implicit def fromStringToPerson(string: String): Person = Person(string)

  "Ryan".greet //this works!  The Compiler replaces it with: fromStringToPerson("Ryan").greet


  //  Implicit classes
  implicit class Dog(name: String) {
    def bark = println("bark!")
  }

  "Mimi".bark // this works too!  The Compiler replaces it with new Dog("Mimi").bark





}
