package com.actionfps.syslog

import java.net.InetSocketAddress

import cats.effect.{IO, Resource}
import fs2._
import fs2.io.udp.Packet
import org.pcap4j.core.{PcapHandle, Pcaps}
import org.pcap4j.packet.{IpV4Packet, UdpPacket}
import org.scalatest.{FreeSpec, Matchers}

object CombinedPcapReaderSpec {

  def getPacket(pcapHandle: PcapHandle): IO[Option[Packet]] = {
    IO.delay {
      for {
        packet <- Option(pcapHandle.getNextPacket)
        ipv4Packet <- Option(packet.get(classOf[IpV4Packet]))
        udpPacket <- Option(packet.get(classOf[UdpPacket]))
      } yield Packet(
        bytes = Chunk.array(udpPacket.getPayload.getRawData),
        remote = InetSocketAddress.createUnresolved(
          ipv4Packet.getHeader.getSrcAddr.getHostAddress,
          udpPacket.getHeader.getSrcPort.valueAsInt()
        )
      )
    }
  }

  def pcapUdpReader(openPcap: => PcapHandle): Stream[IO, Packet] = {
    fs2.Stream
      .resource(Resource.make(IO.delay(openPcap))(pcaps => IO.delay(pcaps.close())))
      .evalMap(getPacket)
      .unNoneTerminate
  }
}

class CombinedPcapReaderSpec extends FreeSpec with Matchers {
  "It works" in {
    CombinedPcapReaderSpec.pcapUdpReader(Pcaps.openOffline("syslog-3.pcap"))
      .take(10)
      .evalMap(p => IO.delay(println(p)))
      .compile
      .drain
      .unsafeRunSync()
  }
}
