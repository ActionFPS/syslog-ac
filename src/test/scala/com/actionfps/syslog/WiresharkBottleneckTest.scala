package com.actionfps.syslog

import java.net.InetAddress

import com.actionfps.syslog.LeagueApp.logger
import org.pcap4j.core.Pcaps
import org.pcap4j.packet.{IpV4Packet, UdpPacket}
import org.productivity.java.syslog4j.server.{SyslogServerEventHandlerIF, SyslogServerEventIF, SyslogServerIF}
import org.productivity.java.syslog4j.server.impl.AbstractSyslogServer
import org.productivity.java.syslog4j.server.impl.event.SyslogServerEvent
import org.scalatest.FreeSpec

final class WiresharkBottleneckTest extends FreeSpec {
  "It works" in {
    var state = EventProcessor.empty
    val pcaps = Pcaps.openOffline("""./port5000.pcap""")
    Iterator.continually(pcaps.getNextPacket).takeWhile(_ != null)
      .zipWithIndex
      .foreach { case (packet, idx) =>
        val udpData = packet.getRawData.drop(42)
        val evt = new SyslogServerEvent(udpData, udpData.length, InetAddress.getLocalHost)
        val scalaEvent = SyslogServerEventIFScala(evt)
        state.process(scalaEvent, EventProcessor.currentTime) match {
          case None =>
          case Some((nep, rm@AcServerMessage(date, serverName, payload))) =>
            state = nep
        }
      }

    info(state.toString.length.toString)
  }
}