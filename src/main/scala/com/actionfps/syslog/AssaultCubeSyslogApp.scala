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
  private val bindUri = new URI(args(0))
  private implicit val grp: AsynchronousSocketGroup = AsynchronousSocketGroup()
  require(bindUri.getScheme == "udp", "Only UDP supported now")
  private val targetPath = Paths.get(args(1))
  require(Files.isWritable(targetPath.getParent), s"Make sure we can write to $targetPath")
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
    .through(packetsToAcServerMessages)
    .through(filterDefiniteAcServerMessages)
    .map(_.toLine)
    .evalMap(appendLine(targetPath))
    .compile
    .drain
    .unsafeRunSync()
}
