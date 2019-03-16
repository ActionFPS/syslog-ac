package com.actionfps.syslog

import java.net.{InetSocketAddress, URI}
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
  private val socketResource = Socket[IO](
    address = new InetSocketAddress(bindUri.getHost, bindUri.getPort)
  )
  fs2.Stream
    .resource(socketResource)
    .flatMap(_.reads())
    .through(packetsToAcServerMessages)
    .through(filterDefiniteAcServerMessages)
    .evalMap(o => IO.delay(println(o)))
    .compile
    .drain
    .unsafeRunSync()
}
