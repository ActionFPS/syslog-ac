package com.actionfps

import java.time.Instant
import cats.effect.IO
import cats.effect.concurrent.Ref
import fs2.io.udp.Packet
import scala.util.matching.Regex

/**
  * Created by William on 25/12/2015.
  */
package object syslog {

  val matchServerStatus: Regex = """Status at [^ ]+ [^ ]+: \d+.*""".r

  val matchPlayerActivity: Regex = """\[\d+\.\d+\.\d+\.\d+\] [^ ]+ (sprayed|busted|gibbed|punctured) [^ ]+""".r

  final case class AcServerMessage(instant: Instant, serverMessage: ServerMessage) {
    def toLine = s"$instant\t${serverMessage.serverId}\t${serverMessage.serverMessage}"
  }

  def checkMessageMatched(serverMessage: ServerMessage): Boolean = {
    matchServerStatus.unapplySeq(serverMessage.serverMessage)
      .orElse(matchPlayerActivity.unapplySeq(serverMessage.serverMessage))
      .isDefined
  }

  final case class ServerMessage(serverId: String, serverMessage: String)

  object ServerMessage {
    def unapply(message: String): Option[ServerMessage] = {
      for {
        indexOfColon <- Option(message.indexOf(':'))
        if indexOfColon > 0
        ' ' <- message.lift(indexOfColon + 1)
      } yield ServerMessage(message.substring(0, indexOfColon), message.substring(indexOfColon + 2))
    }
  }

  /**
    * We receive syslogs from servers other than AC
    * and for our uses we wish to filter those out
    * The right way to determine whether a legit source
    * is sending us data is to detect any AC-specific messages
    * and then based on that filter messages out
    */
  final case class ServerFilterState(validServers: Set[String]) {
    def isValid(serverMessage: ServerMessage): Boolean = validServers.contains(serverMessage.serverId)

    def includePotentiallyValid(serverMessage: ServerMessage): Option[ServerFilterState] = {
      if (checkMessageMatched(serverMessage)) Some(copy(validServers = validServers + serverMessage.serverId))
      else None
    }
  }

  object ServerFilterState {
    val empty = ServerFilterState(validServers = Set.empty)
  }

  import fs2._

  def packetsToAcServerMessages: Pipe[IO, Packet, AcServerMessage] = {
    _.evalMap(packet => IO.delay(Instant.now() -> packet)).map { case (instant, packet) =>
      for {
        syslogMessage <- SyslogMessage.unapply(packet.bytes.toArray)
        serverMessage <- ServerMessage.unapply(syslogMessage.message)
        ipAddress <- packet.remote.toString.split(":").headOption
        newServerId = s"${ipAddress} ${serverMessage.serverId}"
      } yield AcServerMessage(instant, serverMessage.copy(serverId = newServerId))
    }.unNone
  }

  def filterDefiniteAcServerMessages: Pipe[IO, AcServerMessage, AcServerMessage] = {
    q =>
      fs2.Stream.eval(Ref.of[IO, ServerFilterState](ServerFilterState.empty))
        .flatMap { ref =>
          q.evalMap { acMessage =>
            ref.get.flatMap { sfs =>
              if (sfs.isValid(acMessage.serverMessage)) IO.pure(Some(acMessage))
              else {
                sfs.includePotentiallyValid(acMessage.serverMessage) match {
                  case Some(newState) =>
                    ref.set(newState).map(_ => Some(acMessage))
                  case None =>
                    IO.pure(None)
                }
              }
            }
          }
        }.unNone
  }

}
