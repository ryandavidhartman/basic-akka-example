package part1recap

import scala.concurrent.Future
import scala.language.implicitConversions
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

  private val function: Int => Int = pf1
  assert(function(1) == pf2(1))

  val modifiedList = Try { List(1,2,3).map(pf2) }

  assert(modifiedList.isFailure)

  // Define a PF on the fly

  val modifiedList2 = List(1,2,3).map {
    case 1 => 42
    case _ => 0
  }

  assert(modifiedList2 == List(42, 0, 0))

  // Lifting Partial Functions
  private val lifted = pf2.lift  // (Int => Option[Int])
  assert(lifted(1).contains(42))
  assert(lifted(100).isEmpty)

  // Chaining Partial Functions
  val pfChain: PartialFunction[Int, Int] = pf2.orElse[Int, Int] {
    case 99 => 101
  }
  assert(pfChain(1) == 42)
  assert(pfChain.lift(100).isEmpty)
  assert(pfChain(99) == 101)


  // Type aliases
  type ReceiveFunction = PartialFunction[Any, Unit]

  def receive: ReceiveFunction = {
    case 1 => println("Hello")
    case _ => println("Confused...")
  }

  assert(receive(1) == () )

  // Implicit vals
  implicit val timeout: Int = 3000
  def callWithTimeout(f: () => Unit)(implicit timeout: Int): Unit = f()
  callWithTimeout(() => println("hi"))  //works because we have an implicit int in scope!

  // Implicit conversions

  // Implicit defs
  // Given:
  case class Person(name: String) {
    def greet(): Unit = println(s"Hi, my name is $name")
  }

  implicit def fromStringToPerson(string: String): Person = Person(string)

  "Ryan".greet() //this works!  The Compiler replaces it with: fromStringToPerson("Ryan").greet


  //  Implicit classes
  implicit class Dog(name: String) {
    def bark(): Unit = println("bark!")
  }

  "Mimi".bark() // this works too!  The Compiler replaces it with new Dog("Mimi").bark

  // Organizing Implicits
  // implicit in LOCAL SCOPE
  implicit val inverseOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)
  val sorted1 = List(1,2,3).sorted  // takes an implicit sorting parameter
  assert(sorted1 == List(3,2,1))

  // implicit in IMPORTED SCOPE
  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future {
    println("hello, future")
  }

  // implicit in a Companion Object
  object Person {
    implicit val personOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
  }

  val peopleSorted = List(Person("steve"), Person("alice"), Person("bob")).sorted
  assert(peopleSorted == List(Person("alice"), Person("bob"), Person("steve")) )

  // IMPLICIT FETCH ORDER:
  // Local scope, imported scope, companion objects of included types




}
