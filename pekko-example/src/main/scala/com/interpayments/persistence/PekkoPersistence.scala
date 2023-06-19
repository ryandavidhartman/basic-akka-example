package com.interpayments.persistence

import com.interpayments.persistence.Messages._
import org.apache.pekko.Done
import org.apache.pekko.actor._
import org.apache.pekko.pattern.ask
import org.apache.pekko.persistence._
import org.apache.pekko.persistence.jdbc.testkit.scaladsl.SchemaUtils
import org.apache.pekko.util.Timeout

import java.time.LocalDateTime.now
import java.time.ZoneOffset
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}


class ExamplePersistentActor extends PersistentActor with ActorLogging {
  override def persistenceId: String = "example-persistent-actor-1"

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

object PekkoPersistenceExample extends App {

  //#actor-system
  implicit val system: ActorSystem = ActorSystem("pekko-persistence-example")
  val done: Future[Done] = SchemaUtils.createIfNotExists()
  val persistentActor = system.actorOf(Props[ExamplePersistentActor], "my-persistent-actor")

  (0 to 100).foreach(i => persistentActor ! Cmd(s"state", now.minusHours(100-i)))

  persistentActor ! "print"

  implicit val timeout: Timeout = 10 seconds
  val stateInThePast: Future[Any] = persistentActor ? GetStateAtTimestamp(now.minusHours(25))

  stateInThePast.onComplete {
    case Success(state) => println(state)
    case Failure(e) => println(e.getMessage)
  }

}