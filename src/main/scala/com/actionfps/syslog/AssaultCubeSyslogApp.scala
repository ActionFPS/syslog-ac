package com.actionfps.syslog

import cats.effect.{Blocker, ExitCode, IO, IOApp}
import com.actionfps.syslog.StreamProcessors.{currentTime, filterDefiniteAcServerMessages, packetsToAcServerMessages}
import fs2.io.udp.SocketGroup

import java.net.{InetSocketAddress, URI}
import java.nio.file._

object AssaultCubeSyslogApp extends IOApp {

  private val bindUri = new URI(
    System
      .getenv("BIND_URI")
      .ensuring(_ != null, "Set 'BIND_URI'")
  ).ensuring(_.getScheme == "udp", "Only UDP supported now")

  private val targetPath = Paths
    .get(
      System
        .getenv("APPEND_PATH")
        .ensuring(_ != null, "Set 'APPEND_PATH'")
    )
    .ensuring(
      path => Files.isWritable(path.getParent),
      s"Make sure we can write to the path"
    )

  private def appendLine(path: Path)(line: String): IO[Unit] = IO.delay {
    Files.write(path, (line + "\n").getBytes(), StandardOpenOption.APPEND)

    ()
  }

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      blocker <- Blocker[IO]
      socketGroup <- SocketGroup[IO](blocker)
    } yield fs2.Stream
      .resource(socketGroup.open[IO](new InetSocketAddress(bindUri.getHost, bindUri.getPort)))
      .flatMap(_.reads())
      .through(packetsToAcServerMessages(currentTime))
      .through(filterDefiniteAcServerMessages)
      .map(_.toLine)
      .filter(line => !line.contains('\n'))
      .evalMap(appendLine(targetPath))
      .compile
      .drain
  }.use(identity)
    .as(ExitCode.Error)

}
