package part2actors

import akka.actor.ActorSystem

object ActorsIntro extends App {

  // part 1 actor systems
  val actorSystem = ActorSystem("firstActorSystem")  // Note: name must contain only word characters
                                                     // (i.e. [a-zA-Z0-9] plus non-leading '-' or '_')
  println("Actor system: " + actorSystem.name + " started at: " +  actorSystem.startTime)

  // part 2 create some actors

  /*
    1) Actors are uniquely identified withing an actor
    2) Messages are sent and processes asynchronously
    3) Each actor has maybe respond differently to a given message
    4) Actors are encapsulated so that the only interaction you may have with them is by message passing.
  */




}
