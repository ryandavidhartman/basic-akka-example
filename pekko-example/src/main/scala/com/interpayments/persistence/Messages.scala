package com.interpayments.persistence

import java.time.LocalDateTime

object Messages {
  case class Cmd(data: String, ts: LocalDateTime)
  case class State(data: String, ts: LocalDateTime)
  case class GetStateAtTimestamp(time: LocalDateTime)
  case class HistoricalStates(states: List[State] = Nil) {
    def updated(newState: State): HistoricalStates = copy(newState :: states)
    def size: Int = states.length
    override def toString: String = states.reverse.toString
  }
}
