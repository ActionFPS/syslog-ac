package com.actionfps.syslog

import java.net.InetSocketAddress

import cats.effect.{IO, Resource}
import fs2._
import fs2.io.udp.Packet
import org.pcap4j.core.{PcapHandle, Pcaps}
import org.pcap4j.packet.{IpV4Packet, UdpPacket}
import org.scalatest.{FreeSpec, Matchers}

object CombinedPcapReaderSpec {

  def getPacket(pcapHandle: PcapHandle): IO[Option[Option[Packet]]] = {
    IO.delay {
      for {
        packet <- Option(pcapHandle.getNextPacket)
      } yield for {
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
      .flatMap { resource =>
        fs2.Stream.repeatEval(getPacket(resource))
      }
      .unNoneTerminate
      .unNone
  }
}

class CombinedPcapReaderSpec extends FreeSpec with Matchers {

  private val readPackets = CombinedPcapReaderSpec.pcapUdpReader(
    Pcaps.openOffline("src/test/scala/resources/syslog-three.pcap")
  )

  "It reads 3 packets as expected" in {
    readPackets
      .compile
      .toList
      .unsafeRunSync() should have size 3
  }

  "It reads 3 AC messages as expected" in {
    readPackets
      .through(packetsToAcServerMessages)
      .compile
      .toList
      .unsafeRunSync() should have size 3
  }

  "It reads 2 AC messages as filtered" in {
    readPackets
      .through(packetsToAcServerMessages)
      .through(filterDefiniteAcServerMessages)
      .compile
      .toList
      .unsafeRunSync() should have size 2
  }

}
