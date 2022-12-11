package part1recap

import javax.management.InvalidApplicationException
import scala.util.Try

object GeneralRecap extends App {
  private val aCondition: Boolean = false //how to declare an immutable val
  private var aVariable = 1  //how to declare a variable, reassignment is allowed
  aVariable = 2

  // expressions
  private val aConditionalVal: Int = if(aCondition) 42 else 65
  assert(aConditionalVal == 65)

  // code block
  private val aCodeBlock = {
    if(aCondition) 74
    56  // this is the value returned by the expression that is the code block
  }

  assert(aCodeBlock == 56)

  //types
  // Unit
  private val theUnit: Unit = println("Hello, Scala") //Unit is the type of something that just has side effects
  theUnit

  private def aFunction(x: Int): Int = x + 1
  assert(aFunction(4) == 5)
  assert(aFunction(9) == 10)

  private val aAnotherFunction: Int => Int = { x => x+1}

  private val test = aAnotherFunction(5)
  println(test)

  //recursion & tail recursion

  @scala.annotation.tailrec
  private def factorial(n:Int, acc:Int = 1): Int =
    if(n <= 0) acc
    else factorial(n-1, acc*n)

  assert(factorial(3) == 6)

  // OOP
  class Animal
  private class Dog extends Animal
  private val aDog: Animal = new Dog

  private trait Carnivore {
    def eat(a: Animal): Unit
  }

  private class Crocodile extends Animal with Carnivore {
    override def eat(a: Animal): Unit = println("crunch")
  }

  // method notations
  private val aCroc = new Crocodile
  aCroc.eat(aDog)  // same as the following
  aCroc eat aDog

  // anonymous classes - make a new anonymous class by directly
  // implementing a required method for a trait
  private val aCarnivore = new Carnivore {
    override def eat(a: Animal): Unit = "wow an anonymous class"
  }

  aCarnivore eat aDog

  // generics
  abstract class MyList[+T]
  // companion objects
  object MyList

  // case classes
  case class Person(name: String, age: Int)

  // Exceptions
  private val aPotentialException: String = try {
    throw new InvalidApplicationException("failed")  //type Nothing!
  } catch {
    case e: Exception => s"I caught an exception: ${e.getMessage}"
  } finally {
    // side effects
    println("some stuff for the logs")
  }

  // Functional Programming
  // functions are objects in Scala

  private val incrementer1 = (v1:Int) => v1 + 1

  // same as

  private val incrementer2 = new Function[Int, Int] {
    override def apply(v1: Int): Int = v1 + 1
  }

  // i.e. type Int => Int is sugar for the type Function1[Int, Int

  (0 to 10) foreach { i => assert(incrementer1(i) == incrementer2(i)) }

  // FP is about working with functions as first class data types

  List(1,2,3).map(incrementer1) //works since map is a HIGHER ORDER FUNCTION (HOF) and takes a function
  // as a parameter

  // for comprehensions

  private val pairs1 = for {
    num <- List(1,2,3)
    char <- List ('a', 'b', 'c')
  } yield num + "-" + char

  private val pairs2 = List(1,2,3).flatMap(num => List('a', 'b',  'c').map(char => num + "-" + char))

  assert(pairs1 == pairs2)

  // Scala collections Seq, Array, Vector, Map, Tuples and Sets

  // Other "collections" Options and Try
  private val anOption = Some(2)
  assert(anOption.isDefined)
  private val aTry = Try(throw new RuntimeException("ouch1!"))
  assert(aTry.isFailure)


  // Pattern Matching
  private val unknown = 2
  private val order = unknown match {
    case 1 => "first"
    case 2 => "second"
    case _ => "something else"
  }

  assert(order == "second")

}
