package com.actionfps.syslog

import cats.effect.{IO, Resource}
import com.actionfps.syslog.StreamProcessors.{
  currentTime,
  filterDefiniteAcServerMessages,
  packetsToAcServerMessages
}
import fs2._
import fs2.io.udp.Packet
import org.pcap4j.core.{PcapHandle, Pcaps}
import org.pcap4j.packet.{IpV4Packet, UdpPacket}
import org.scalatest.freespec.AnyFreeSpec

import java.net.InetSocketAddress
import java.time.Instant

object CombinedPcapReaderSpec {

  def getPacket(pcapHandle: PcapHandle): IO[Option[Option[Packet]]] =
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

  def pcapUdpReader(openPcap: => PcapHandle): Stream[IO, Packet] =
    fs2.Stream
      .resource(Resource.fromAutoCloseable(IO.delay(openPcap)))
      .flatMap { pcapHandle =>
        fs2.Stream.repeatEval(getPacket(pcapHandle))
      }
      .unNoneTerminate
      .unNone
}

final class CombinedPcapReaderSpec extends AnyFreeSpec {

  private val readPackets = CombinedPcapReaderSpec.pcapUdpReader(
    Pcaps.openOffline("src/test/scala/resources/syslog-three.pcap")
  )

  "It reads 3 packets as expected" in {
    assert(
      readPackets.compile.toList
        .unsafeRunSync()
        .size == 3
    )
  }

  "It reads 3 AC messages as expected" in {
    assert(
      readPackets
        .through(packetsToAcServerMessages(currentTime))
        .compile
        .toList
        .unsafeRunSync()
        .size == 3
    )
  }

  "It reads 2 AC messages as filtered" in {
    assert(
      readPackets
        .through(packetsToAcServerMessages(currentTime))
        .through(filterDefiniteAcServerMessages)
        .compile
        .toList
        .unsafeRunSync()
        .size == 2
    )
  }

  "It renders the message as expected" in {
    assert(
      readPackets
        .through(
          packetsToAcServerMessages(
            IO.pure(Instant.parse("2019-03-16T20:55:53.250304800Z"))
          )
        )
        .through(filterDefiniteAcServerMessages)
        .map(_.toLine)
        .compile
        .last
        .unsafeRunSync()
        .contains(
          "2019-03-16T20:55:53.250304800Z\t157.230.139.74 bluf AssaultCube[gaulpublic]\tTeam  CLA:  2 players,   24 frags,    1 flags"
        )
    )
  }

}
