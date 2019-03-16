package com.actionfps.syslog

import java.net.{InetSocketAddress, URI}
import java.nio.file._

import cats.effect.{ContextShift, IO}
import com.typesafe.scalalogging.StrictLogging
import fs2._
import fs2.io.udp.{AsynchronousSocketGroup, Socket}

import scala.concurrent.ExecutionContext

object AssaultCubeSyslogApp extends App with StrictLogging {
  private implicit val contextShiftIO: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  private val bindUri = new URI(
    System.getenv("BIND_URI")
      .ensuring(_ != null, "Set 'BIND_URI'")
  )
    .ensuring(_.getScheme == "udp", "Only UDP supported now")
  private val targetPath = Paths.get(
    System.getenv("APPEND_PATH")
      .ensuring(_ != null, "Set 'APPEND_PATH'")
  ).ensuring(path =>
    Files.isWritable(path.getParent), s"Make sure we can write to the path")

  private implicit val grp: AsynchronousSocketGroup = AsynchronousSocketGroup()

  private val socketResource = Socket[IO](
    address = new InetSocketAddress(bindUri.getHost, bindUri.getPort)
  )

  def appendLine(path: Path)(line: String): IO[Unit] = {
    IO.delay {
      Files.write(path, (line + "\n").getBytes(), StandardOpenOption.APPEND)
    }
  }

  fs2.Stream
    .resource(socketResource)
    .flatMap(_.reads())
    .through(packetsToAcServerMessages(currentTime))
    .through(filterDefiniteAcServerMessages)
    .map(_.toLine)
    .evalMap(appendLine(targetPath))
    .compile
    .drain
    .unsafeRunSync()
}
