package com.interpayments.persistence

import com.interpayments.persistence.Messages._
import org.apache.pekko.Done
import org.apache.pekko.actor._
import org.apache.pekko.pattern.ask
import org.apache.pekko.persistence._
import org.apache.pekko.persistence.jdbc.query.scaladsl.JdbcReadJournal
import org.apache.pekko.persistence.jdbc.testkit.scaladsl.SchemaUtils
import org.apache.pekko.persistence.query.PersistenceQuery
import org.apache.pekko.stream.{ActorMaterializer, Materializer}
import org.apache.pekko.stream.scaladsl.Sink
import org.apache.pekko.util.Timeout

import java.time.LocalDateTime.now
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success}

class ExamplePersistentActor2 extends PersistentActor with ActorLogging {
  override def persistenceId: String = "example-persistent-actor-2"

  var currentState: State = State("Initial State", now)
  def numEvents: Long = lastSequenceNr
  val snapShotInterval = 10
  def updateState(event: State): Unit = currentState = event

  val receiveRecover: Receive = {
    case evt: State => updateState(evt)
    case SnapshotOffer(_, snapshot: State) =>
      currentState = snapshot
    case RecoveryCompleted =>
      log.info("Recovery completed.")
  }

  val receiveCommand: Receive = {
    case Cmd(data, ts) =>
      persist(State(s"$data-$numEvents", ts)) { event =>
        updateState(event)
        context.system.eventStream.publish(event)
        if (lastSequenceNr % snapShotInterval == 0 && lastSequenceNr != 0)
          saveSnapshot(currentState)
      }

    case GetStateAtTimestamp(time) =>
      //val originalSender = context.sender()
      //val originalState = currentState

      val sequenceNr = 50 //lastSequenceNr / 2

      val bob = queryJournalForPreviousState(sequenceNr)

      val stateAtTimestamp = State("not implemented", now)
      //currentState = originalState
      sender ! stateAtTimestamp

    case "print" => println(currentState)
  }

  def queryJournalForPreviousState(seqNr: Long) = {
    implicit val materializer: Materializer = Materializer(context.system)

    println(s"Looking at seq number: $seqNr")

    // Initialize the JdbcReadJournal
    val readJournal: JdbcReadJournal = PersistenceQuery(context.system).readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)

    // Query the journal for the state of the actor at the given sequence number
    readJournal.eventsByPersistenceId(persistenceId, 0, seqNr+1)
      .runWith(Sink.foreach(i => println(s"hi hi $i")))
      .onComplete { _ =>
        // Shutdown the ActorSystem after the query completes
        println("DONE BITCH")
      }
  }

}

object PekkoPersistenceExample2 extends App {

  //#actor-system
  implicit val system: ActorSystem = ActorSystem("pekko-persistence-example2")
  val done: Future[Done] = SchemaUtils.createIfNotExists()
  val persistentActor = system.actorOf(Props[ExamplePersistentActor2], "my-persistent-actor-2")

  (0 to 100).foreach(i => persistentActor ! Cmd(s"state", now.minusHours(100-i)))

  persistentActor ! "print"

  implicit val timeout: Timeout = 10 seconds
  val stateInThePast: Future[Any] = persistentActor ? GetStateAtTimestamp(now.minusHours(25))

  stateInThePast.onComplete {
    case Success(state) => println(state)
    case Failure(e) => println(e.getMessage)
  }

  // Shutdown the ActorSystem after the query completes
  //CoordinatedShutdown(system).run(CoordinatedShutdown.JvmExitReason)

}
