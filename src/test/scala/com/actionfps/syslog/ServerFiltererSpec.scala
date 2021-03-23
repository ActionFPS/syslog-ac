package com.actionfps.syslog

import com.actionfps.syslog.ServerFiltererSpec._
import org.scalatest.OptionValues._
import org.scalatest.freespec.AnyFreeSpec

import java.time.Instant

object ServerFiltererSpec {
  private val GoodMessage =
    "bluf AssaultCube[gaulpublic]: [23.243.193.16] Houdini busted jack99"

  private val GoodAcServerMessage = AcServerMessage(
    instant = Instant.parse("2019-03-16T19:40:11.552372200Z"),
    serverMessage = ServerMessage(
      "157.230.139.74 bluf AssaultCube[gaulpublic]",
      "Status at 15-03-2019 09:27:54: 7 remote clients, 15.8 send, 4.4 rec (K/sec); Ping: #84|4108|270; CSL: #23|207|46 (bytes)"
    )
  )
}

final class ServerFiltererSpec extends AnyFreeSpec {
  "It extracts the server name" in {
    assert(
      ServerMessage
        .fromMessageString(GoodMessage)
        .map(_.serverId)
        .contains("bluf AssaultCube[gaulpublic]")
    )
  }

  "It extracts the message" in {
    assert(
      ServerMessage
        .fromMessageString(GoodMessage)
        .map(_.serverMessage)
        .contains("[23.243.193.16] Houdini busted jack99")
    )
  }

  "It matches a match" in {
    assert(
      ServerMessage(
        serverId = "???",
        serverMessage = "[23.243.193.16] Houdini busted jack99"
      ).checkMessageMatched
    )
  }

  "It matches a Status" in {
    assert(GoodAcServerMessage.serverMessage.checkMessageMatched)
  }

  "The message is considered valid" in {
    assert(
      ServerFilterState.empty
        .includePotentiallyValid(GoodAcServerMessage.serverMessage)
        .value
        .isValid(GoodAcServerMessage.serverMessage)
    )
  }
}
