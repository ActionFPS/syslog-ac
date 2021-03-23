# syslog-ac

Syslog service for ActionFPS/AssaultCube game servers.

This application acts as a service that listens to the syslog protocol and parses out the messages that match the
AssaultCube game log format. After verifying the inputs, it appends the logs to a timestamped TSV file in the format
of time + server id + message, which is easy to consume for the ActionFPS log parser.

It is written in Scala 3. The stack is using Scala's FS2 functional streaming library to listen on UDP.
For testing, we use ScalaTest + Pcap4j to verify parsing at the packet level.

There is a sample pcap file in the test resources.

## Function

Works in both UDP and TCP modes. Here's some rsyslog.d config:

```
# cat /etc/rsyslog.d/60-ac.conf
*.* @87.98.216.121:5000
*.* @@87.98.216.121:6000
```

`@` for UDP, `@@` for TCP.

## Building

For pcap, check the set-up instructions. You may need to install a native library.
https://github.com/kaitoy/pcap4j

```
$ git fetch --tags
$ sbt test rpm:packageBin # CentOS
```
