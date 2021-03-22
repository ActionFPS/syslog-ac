# syslog-ac

Syslog service for ActionFPS/AssaultCube game servers.

Migrated from <a href="https://github.com/ScalaWilliam/ActionFPS">ActionFPS</a> as this component is orthogonal to the
rest for the time being.

Works in both UDP and TCP modes. Here's some rsyslog.d config:

```
# cat /etc/rsyslog.d/60-ac.conf
*.* @87.98.216.121:5000
*.* @@87.98.216.121:6000
```

`@` for UDP, `@@` for TCP.

## Building

```
$ git fetch --tags
$ sbt test rpm:packageBin # CentOS
```

For pcap, check the set-up instructions. 
https://github.com/kaitoy/pcap4j#winpcap-or-npcap
