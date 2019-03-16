package com.actionfps.syslog

import java.time.Instant

import org.scalatest.FreeSpec
import org.scalatest.Matchers._
import org.scalatest.OptionValues._

final class ServerFiltererSpec extends FreeSpec {
  private val sampleMessage = """aura AssaultCube[local#1999]: demo written to file "demos/20190316_1419_local_ac_gothic_8min_DM.dmo" (162438 bytes)"""
  val goodMessage = "bluf AssaultCube[gaulpublic]: [23.243.193.16] Houdini busted jack99"
  "It extracts server name" in {
    ServerMessage.unapply(goodMessage).value.serverId shouldBe "bluf AssaultCube[gaulpublic]"
    ServerMessage.unapply(goodMessage).value.serverMessage shouldBe "[23.243.193.16] Houdini busted jack99"
  }

  "It matches a match" in {
    assert(checkMessageMatched(ServerMessage(serverId = "???", serverMessage = "[23.243.193.16] Houdini busted jack99")))
  }
  val sampleMessage2 = AcServerMessage(
    instant = Instant.parse("2019-03-16T19:40:11.552372200Z"),
    serverMessage = ServerMessage("157.230.139.74 bluf AssaultCube[gaulpublic]",
      "Status at 15-03-2019 09:27:54: 7 remote clients, 15.8 send, 4.4 rec (K/sec); Ping: #84|4108|270; CSL: #23|207|46 (bytes)")
  )

  "It matches a Status" in {
    assert(checkMessageMatched(sampleMessage2.serverMessage))
  }

  "It works" in {
    ServerFilterState.empty.includePotentiallyValid(sampleMessage2.serverMessage)
      .value.isValid(sampleMessage2.serverMessage) shouldBe true
  }

}
