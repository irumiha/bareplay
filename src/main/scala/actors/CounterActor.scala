package actors

import org.apache.pekko.actor.Actor
import models.{AccessCounterRepository, AccessCounterRow}
import play.api.Logging

import scala.concurrent.ExecutionContext.Implicits.global

object CounterActor:
  trait Tag

  case class IncrementCounter(counterId: Long)

class CounterActor(accessCounterRepository: AccessCounterRepository)
    extends Actor
    with Logging:
  import CounterActor.*
  var longCounter: Long = 0

  logger.info("Starting Counter Actor")

  override def receive: Receive = { case IncrementCounter(counterId) =>
    val replyTo = sender()

    longCounter += 1

    if longCounter % 1000 == 0 then
      accessCounterRepository
        .persistExisting(AccessCounterRow(counterId, longCounter))
        .foreach(result => replyTo ! result)
    else replyTo ! AccessCounterRow(1, longCounter)
  }
