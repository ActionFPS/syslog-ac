package com.actionfps.syslog

/**
  * We receive syslogs from servers other than AC
  * and for our uses we wish to filter those out
  * The right way to determine whether a legit source
  * is sending us data is to detect any AC-specific messages
  * and then based on that filter messages out
  */
final case class ServerFilterState(validServers: Set[String]) {
  def isValid(serverMessage: ServerMessage): Boolean =
    validServers.contains(serverMessage.serverId)

  def includePotentiallyValid(
      serverMessage: ServerMessage
  ): Option[ServerFilterState] =
    Option.when(serverMessage.checkMessageMatched)(
      copy(validServers = validServers + serverMessage.serverId)
    )
}

object ServerFilterState {
  val empty: ServerFilterState = ServerFilterState(validServers = Set.empty)
}
