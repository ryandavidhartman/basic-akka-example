package com.interpayments

//import org.apache.pekko.actor._
//import org.apache.pekko.persistence
//import org.apache.pekko.persistence._
//import org.apache.pekko.persistence.serialization.Snapshot

import akka.actor._
import akka.persistence
import akka.persistence._
import akka.persistence.serialization.Snapshot
import akka.persistence.Recovery

import java.time.LocalDateTime.now
import java.time.{LocalDateTime, ZoneOffset}

case class Cmd(data: String)
case class Evt(data: String)

case class ExampleState(events: List[String] = Nil) {
  def updated(evt: Evt): ExampleState = copy(evt.data :: events)
  def size: Int = events.length
  override def toString: String = events.reverse.toString
}

class ExamplePersistentActor extends PersistentActor with ActorLogging {
  override def persistenceId: String = "sample-id-1"

  var state = ExampleState()

  def updateState(event: Evt): Unit =
    state = state.updated(event)

  def numEvents: Int = state.size

  val receiveRecover: Receive = {
    case evt: Evt => updateState(evt)
    case SnapshotOffer(_, snapshot: ExampleState) => state = snapshot
  }

  val snapShotInterval = 1000

  val receiveCommand: Receive = {
    case Cmd(data) =>
      persist(Evt(s"$data-$numEvents")) { event =>
        updateState(event)
        context.system.eventStream.publish(event)
        if (lastSequenceNr % snapShotInterval == 0 && lastSequenceNr != 0)
          saveSnapshot(state)
      }

    case "print" => println(state)
  }
}

object PekkoPeristenceExample extends App {

  //#actor-system
  val system: ActorSystem = ActorSystem("pekko-persistence-example")
  val persistentActor = system.actorOf(Props[ExamplePersistentActor], "my-persistent-actor")

  (0 to 100).foreach(i => persistentActor ! Cmd(s"state-$i"))

  persistentActor ! "print"


}

/*
import akka.persistence.{ Snapshot, Recovery }



def findStateAtTime(persistenceId: String, time: LocalDateTime): Option[State] = {

  // Get the snapshot or events that were persisted for the object.

  val snapshot: Option[Snapshot] = persistence.snapshot.get(persistenceId)
  val events: Seq[Event] = persistence.recovery.get(persistenceId)



  // Replay the events to reconstruct the state of the object.
  val state = events.foldLeft(State()) { (state, event) =>
    event.apply(state)
  }



  // Find the state of the object at the desired time.
  val desiredState = state.find(_.timestamp == time)

 // Return the desired state.
 desiredState

}
 */

/*
import akka.actor.ActorSystem
import akka.persistence._

object SnapshotUtils {
  def findStateAtTime(system: ActorSystem, persistenceId: String, time: LocalDateTime): Option[ExampleState] = {
    val snapshotCriteria = SnapshotSelectionCriteria.Latest
    val persistence = Persistence(system)
    val snapshot: Option[ExampleState] = persistence.snapshotStore
      .loadAsync(persistenceId, snapshotCriteria, Long.MaxValue)
      .map(_.asInstanceOf[ExampleState])
      .value
      .getOrElse(None)

    val events: Seq[ExampleState] = persistence
      .journalFor(persistenceId)
      .currentEventsByPersistenceId(persistenceId, 0L, Long.MaxValue)
      .map(_.event.asInstanceOf[ExampleState])
      .toList

    val state = events.foldLeft(State()) { (currentState, event) =>
      event.applyTo(currentState)
    }

    val desiredState = state.find(_.timestamp == time)
    desiredState
  }
}

 */
