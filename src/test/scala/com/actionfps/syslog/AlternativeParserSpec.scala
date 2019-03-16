package com.actionfps.syslog

import javax.xml.bind.DatatypeConverter
import org.scalatest.FreeSpec
import org.scalatest.Matchers._
import org.scalatest.OptionValues._

final class AlternativeParserSpec extends FreeSpec {

  private val inputData = "3c3138323e4d61722031362031353a31393a323320617572612041737361756c74437562655b6c6f63616c23313939395d3a2064656d6f207772697474656e20746f2066696c65202264656d6f732f32303139303331365f313431395f6c6f63616c5f61635f676f746869635f386d696e5f444d2e646d6f22202831363234333820627974657329"
  private val bytes = javax.xml.bind.DatatypeConverter.parseHexBinary(inputData)
  private val dateStr = "Mar 16 15:19:23"
  private val msg = """aura AssaultCube[local#1999]: demo written to file "demos/20190316_1419_local_ac_gothic_8min_DM.dmo" (162438 bytes)"""

  "It parses with Scala directly" in {
    SyslogMessage.unapply(bytes).value.facility shouldBe 176
    SyslogMessage.unapply(bytes).value.level shouldBe 6
    SyslogMessage.unapply(bytes).value.dateStr shouldBe dateStr
    SyslogMessage.unapply(bytes).value.message shouldBe msg
  }

  "Second one" - {
    val inputHex = "3C33303E4D61722031352030393A32363A353420626C75662061737361756C74637562655F7365727665722E7265616C5B393933355D3A204D61722031352030393A32363A3534205465616D20525653463A20203420706C61796572732C20202031342066726167732C202020203220666C616773"
    val inputBytes = DatatypeConverter.parseHexBinary(inputHex)
    val xmsg = "bluf assaultcube_server.real[9935]: Mar 15 09:26:54 Team RVSF:  4 players,   14 frags,    2 flags"
    val facility = 24
    val level = 6
    "Parses Scala" in {
      val msg = SyslogMessage.unapply(inputBytes).value

      msg.facility shouldBe facility
      msg.level shouldBe level
      msg.message shouldBe xmsg
      msg.dateStr shouldBe "Mar 15 09:26:54"
    }
  }

}
