package com.actionfps.syslog

import java.util.Date

import org.scalatest.{FunSuite, Matchers}

class MatcherTest
  extends FunSuite
    with Matchers {

  val sampleMessage = """XServer: 62-210-131-155.rev.poneytelecom.eu aura AssaultCube[local#2999], Payload: Status at 07-05-2015 16:00:33: 0 remote clients, 0.0 send, 0.0 rec (K/sec); Ping: #208|7105|212; CSL: #408|3095|1224 (bytes)"""

  /**
    * echo -n 'XServer: 62-210-131-155.rev.poneytelecom.eu aura AssaultCube[local#2999], Payload: Status at 07-05-2015 16:00:33: 0 remote clients, 0.0 send, 0.0 rec (K/sec); Ping: #208|7105|212; CSL: #408|3095|1224 (bytes)' | nc -u -w1 127.0.0.1 6000
    */
  test("Matcher works") {
    sampleMessage match {
      case extractServerNameStatus(sid, _) => sid shouldBe "XServer: 62-210-131-155.rev.poneytelecom.eu aura AssaultCube[local#2999], Payload"
    }
  }

  def sysM = SyslogServerEventIFScala(
    facility = 0, date = None, level = 4, host = Option("wat"), message = sampleMessage
  )

  test("EventProcessor works") {
    EventProcessor.empty.process(
      fm = sysM,
      newDate = EventProcessor.currentTime
    ) should not be empty
  }

  test("Another one works") {
    val stuff = SyslogServerEventIFScala(24,Some(new Date()),6,Some("localhost"),"sd-55104 ac_server[18480]: May 03 07:36:10 Status at 03-05-2017 07:36:10: 1 remote clients, 0.1 send, 0.5 rec (K/sec); Ping: #15|936|18; CSL: #27|816|81 (bytes)")
    EventProcessor.empty.process(
      fm = stuff,
      newDate = EventProcessor.currentTime
    ) should not be empty
  }

}