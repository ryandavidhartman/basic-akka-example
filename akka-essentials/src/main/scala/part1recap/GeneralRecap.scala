package part1recap

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
}