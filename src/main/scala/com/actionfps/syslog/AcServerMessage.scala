package com.actionfps.syslog

import java.time.Instant

final case class AcServerMessage(
    instant: Instant,
    serverMessage: ServerMessage
) {
  def toLine =
    s"$instant\t${serverMessage.serverId}\t${serverMessage.serverMessage}"
}

object AcServerMessage {}
