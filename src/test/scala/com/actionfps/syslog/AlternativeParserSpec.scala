package com.actionfps.syslog

import java.net.InetAddress

import javax.xml.bind.DatatypeConverter
import org.pcap4j.core.Pcaps
import org.productivity.java.syslog4j.server.SyslogServerEventIF
import org.productivity.java.syslog4j.server.impl.event.SyslogServerEvent
import org.scalatest.FreeSpec
import org.scalatest.Matchers._
import org.scalatest.OptionValues._

final class AlternativeParserSpec extends FreeSpec {

  private val inputData = "3c3138323e4d61722031362031353a31393a323320617572612041737361756c74437562655b6c6f63616c23313939395d3a2064656d6f207772697474656e20746f2066696c65202264656d6f732f32303139303331365f313431395f6c6f63616c5f61635f676f746869635f386d696e5f444d2e646d6f22202831363234333820627974657329"
  private val bytes = javax.xml.bind.DatatypeConverter.parseHexBinary(inputData)
  private val dateStr = "Mar 16 15:19:23"
  private val msg = """aura AssaultCube[local#1999]: demo written to file "demos/20190316_1419_local_ac_gothic_8min_DM.dmo" (162438 bytes)"""

  "It parses with syslog4j" in {
    val event: SyslogServerEventIF = new SyslogServerEvent(bytes, bytes.length, InetAddress.getLocalHost)
    event.getFacility shouldBe 176
    //    event.getHost shouldBe "xyz"
    event.getLevel shouldBe 6
    //    event.getDate shouldBe new java.util.Date("Sat Mar 16 15:19:23 CET 2019")
    event.getMessage shouldBe msg
  }

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
    "Parses syslog4j" in {
      val evt = new SyslogServerEvent(inputBytes, inputBytes.length, InetAddress.getLocalHost)
      evt.getFacility shouldBe facility
      evt.getLevel shouldBe level
      evt.getMessage shouldBe xmsg
    }
    "Parses Scala" in {
      val msg = SyslogMessage.unapply(inputBytes).value

      msg.facility shouldBe facility
      msg.level shouldBe level
      msg.message shouldBe xmsg
      msg.dateStr shouldBe "Mar 15 09:26:54"
    }
  }

  "It parses messages all the same way" in {
    var state = EventProcessor.empty
    val pcaps = Pcaps.openOffline("""syslog-3.pcap""")
    try Iterator.continually(pcaps.getNextPacket).takeWhile(_ != null)
      .zipWithIndex
      .filterNot(_._2 == 37445)
      .foreach { case (packet, idx) =>
        val byteList = packet.getRawData.drop(42).toList
        val event: SyslogServerEventIF = new SyslogServerEvent(byteList.toArray, byteList.length, InetAddress.getLocalHost)
        try {
          val foundLog = SyslogMessage.unapply(byteList.toArray).value
          foundLog.facility shouldEqual event.getFacility
          foundLog.level shouldEqual event.getLevel
          foundLog.message shouldEqual event.getMessage
        }
        catch {
          case e: Exception =>
            info(s"Here, message is: ${event.getMessage}")
            fail(s"Failed here: index ${idx}; ${DatatypeConverter.printHexBinary(byteList.toArray)}")
        }
      } finally pcaps.close()

  }

}
