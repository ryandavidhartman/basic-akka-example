package com.interpayments.persistence

import com.interpayments.persistence.Messages._
import org.apache.pekko.actor._
import org.apache.pekko.pattern.ask
import org.apache.pekko.persistence._
import org.apache.pekko.util.Timeout

import java.time.LocalDateTime.now
import java.time.ZoneOffset
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class ExamplePersistentActor2 extends PersistentActor with ActorLogging {
  override def persistenceId: String = "example-persistent-actor-2"

  var historicalStates: HistoricalStates = HistoricalStates()
  def updateState(event: State): Unit =
    historicalStates = historicalStates.updated(event)

  def numEvents: Int = historicalStates.size

  val receiveRecover: Receive = {
    case evt: State => updateState(evt)
    case SnapshotOffer(_, snapshot: HistoricalStates) => historicalStates = snapshot
  }

  val snapShotInterval = 10

  val receiveCommand: Receive = {
    case Cmd(data, ts) =>
      persist(State(s"$data-$numEvents", ts)) { event =>
        updateState(event)
        context.system.eventStream.publish(event)
        if (lastSequenceNr % snapShotInterval == 0 && lastSequenceNr != 0)
          saveSnapshot(historicalStates)
      }

    case GetStateAtTimestamp(time) =>
      val stateAtTimestamp = Try {
        val eventsBeforeCutoff = historicalStates.states.filter(_.ts.toEpochSecond(ZoneOffset.UTC) < time.toEpochSecond(ZoneOffset.UTC))
        eventsBeforeCutoff.head
      } getOrElse HistoricalStates()
      sender ! stateAtTimestamp

    case "print" => println(historicalStates)
  }

}

object PekkoPersistenceExample2 extends App {

  //#actor-system
  val system: ActorSystem = ActorSystem("pekko-persistence-example2")
  val persistentActor = system.actorOf(Props[ExamplePersistentActor2], "my-persistent-actor-2")

  (0 to 100).foreach(i => persistentActor ! Cmd(s"state", now.minusHours(100-i)))

  persistentActor ! "print"

  implicit val timeout: Timeout = (10 seconds)
  val stateInThePast: Future[Any] = persistentActor ? GetStateAtTimestamp(now.minusHours(25))

  stateInThePast.onComplete {
    case Success(state) => println(state)
    case Failure(e) => println(e.getMessage)
  }

}

/*
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer}

// Step 2: Define the persistent actor
class MyPersistentActor extends PersistentActor with ActorLogging {
  // Step 3: Define state and events
  case class MyState(data: List[String] = Nil)

  var state = MyState()

  override def persistenceId: String = "my-persistent-actor"

  // Step 4: Handle commands
  override def receiveCommand: Receive = {
    case AddData(data) =>
      // Step 6: Persist events
      persist(DataAdded(data)) { event =>
        state = applyEvent(event)
        log.info(s"Data '$data' added.")
      }
    case PrintData =>
      log.info(s"Current data: ${state.data}")
    case RevertToSnapshot(snapshotId) =>
      // Step 8: Revert to a previous state
      deleteSnapshots(SnapshotSelectionCriteria(maxSequenceNr = snapshotId))
    case _ =>
      log.info("Unknown command.")
  }

  // Step 5: Handle events
  override def receiveRecover: Receive = {
    case event: DataAdded =>
      state = applyEvent(event)
    case SnapshotOffer(_, snapshot: MyState) =>
      state = snapshot
    case RecoveryCompleted =>
      log.info("Recovery completed.")
  }

  def applyEvent(event: Event): MyState = {
    event match {
      case DataAdded(data) =>
        state.copy(data = data :: state.data)
    }
  }

  // Step 3: Define events
  sealed trait Event
  case class DataAdded(data: String) extends Event

  // Step 4: Define commands
  sealed trait Command
  case class AddData(data: String) extends Command
  case object PrintData extends Command
  case class RevertToSnapshot(snapshotId: Long) extends Command
}

object Main extends App {
  val system = ActorSystem("PersistenceExample")
  val persistentActor = system.actorOf(Props[MyPersistentActor], "myPersistentActor")

  // Step 4: Send commands to the persistent actor
  persistentActor ! AddData("Data 1")
  persistentActor ! AddData("Data 2")
  persistentActor ! PrintData

  // Step 8: Revert to a previous state
  persistentActor ! RevertToSnapshot(1)

  // After reverting, the actor will be in the state before adding "Data 2"
  persistentActor ! PrintData

  system.terminate()
}

 */
