package com.actionfps.syslog


final case class SyslogMessage(facility: Int, level: Int, message: String, dateStr: String)

object SyslogMessage {
  private def parseIntSafe(str: String): Option[Int] = {
    try Some(java.lang.Integer.parseInt(str))
    catch {
      case _: NumberFormatException => None
    }
  }

  def unapply(input: Array[Byte]): Option[SyslogMessage] = {
    for {
      '<' <- input.headOption
      endPos = input.indexOf('>'.toByte)
      if endPos > -1 && endPos <= 4
      priorityStr = new String(input.slice(1, endPos))
      priority <- parseIntSafe(priorityStr)
      facility = (priority >> 3) << 3
      message = new String(input.drop(endPos + 1))
      if message.length >= 16
      ' ' <- message.lift(3)
      ' ' <- message.lift(6)
    } yield SyslogMessage(facility = facility,
      message = message.substring(16),
      dateStr = message.substring(0, 15),
      level = priority - facility)
  }
}
