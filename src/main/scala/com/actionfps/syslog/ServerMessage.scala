package com.actionfps.syslog

import fs2.io.udp.Packet

import scala.util.matching.Regex

final case class ServerMessage(serverId: String, serverMessage: String) {
  import ServerMessage._
  def checkMessageMatched: Boolean = {
    matchServerStatus
      .unapplySeq(serverMessage)
      .orElse(matchPlayerActivity.unapplySeq(serverMessage))
      .orElse(gameStart.unapplySeq(serverMessage))
      .orElse(matchServerStart.unapplySeq(serverMessage))
      .isDefined
  }
}

object ServerMessage {

  private val matchServerStart: Regex =
    """logging local AssaultCube server.*""".r
  private val gameStart: Regex = """Game start: .*""".r
  private val matchServerStatus: Regex = """Status at [^ ]+ [^ ]+: \d+.*""".r
  private val matchPlayerActivity: Regex =
    """\[\d+\.\d+\.\d+\.\d+\] [^ ]+ (sprayed|busted|gibbed|punctured) [^ ]+""".r

  def fromMessageString(message: String): Option[ServerMessage] = {
    for {
      indexOfColon <- Option(message.indexOf(':'))
      if indexOfColon > 0
      ' ' <- message.lift(indexOfColon + 1)
    } yield ServerMessage(
      message.substring(0, indexOfColon),
      message.substring(indexOfColon + 2)
    )
  }
  def unapply(packet: Packet): Option[ServerMessage] =
    for {
      syslogMessage <- SyslogMessage.unapply(packet.bytes.toArray)
      serverMessage <- ServerMessage.fromMessageString(syslogMessage.message)
      ipAddress <-
        packet.remote.getHostName
          .split(":")
          .headOption
          .map(_.replace("/", "").replace("/", ""))
      newServerId = s"$ipAddress ${serverMessage.serverId}"
    } yield serverMessage.copy(serverId = newServerId)

}
