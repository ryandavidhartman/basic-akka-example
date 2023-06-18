package com.interpayments

import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import Greeter.Greet
import Greeter.Greeted
import org.scalatest.wordspec.AnyWordSpecLike


class PekkoQuickstartSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "A Greeter" must {
    "reply to greeted" in {
      val replyProbe = createTestProbe[Greeted]()
      val underTest = spawn(Greeter())
      underTest ! Greet("Santa", replyProbe.ref)
      replyProbe.expectMessage(Greeted("Santa", underTest.ref))
    }
  }

}
