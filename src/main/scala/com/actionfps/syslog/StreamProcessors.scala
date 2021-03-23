package com.actionfps.syslog

import cats.effect.IO
import cats.effect.concurrent.Ref
import fs2.io.udp.Packet

import java.time.Instant

object StreamProcessors {

  import fs2._

  private[syslog] val currentTime: IO[Instant] = IO.delay(Instant.now())

  def packetsToAcServerMessages(currentTime: IO[Instant] = currentTime): Pipe[IO, Packet, AcServerMessage] =
    _.evalMap(packet => currentTime.map(t => t -> packet)).collect {
      case (instant, ServerMessage(serverMessage)) =>
        AcServerMessage(instant, serverMessage)
    }

  def filterDefiniteAcServerMessages
  : Pipe[IO, AcServerMessage, AcServerMessage] = { acServerMessagesStream =>
    for {
      ref <- fs2.Stream
        .eval(Ref.of[IO, ServerFilterState](ServerFilterState.empty))
      acMessage <- acServerMessagesStream
      serverFilterState <- fs2.Stream.eval(ref.get)
      result <-
        if serverFilterState.isValid(acMessage.serverMessage) then
          fs2.Stream.apply(acMessage)
        else
          fs2.Stream.emits(serverFilterState.includePotentiallyValid(acMessage.serverMessage).toList)
            .evalMap(newState => ref.set(newState))
            .as(acMessage)
    }
      yield result
  }
}

