package com.actionfps.syslog

import org.scalatest.FreeSpec

object ServerFiltererSpec {

}

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
}
