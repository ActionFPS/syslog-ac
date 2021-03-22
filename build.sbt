scalaVersion := "2.13.5"
scalacOptions := Seq(
  "-unchecked", "-deprecation", "-encoding", "utf8"
)
name := "actionfps-syslog"

enablePlugins(JavaAppPackaging)
enablePlugins(RpmPlugin)
enablePlugins(GitVersioning)
enablePlugins(SystemdPlugin)

rpmVendor := "actionfps"
rpmBrpJavaRepackJars := true
rpmLicense := Some("BSD")

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.pcap4j" % "pcap4j-sample" % "2.0.0-alpha.6" % Test,
  "org.scalatest" %% "scalatest" % "3.2.6" % Test,
  "javax.xml.bind" % "jaxb-api" % "2.3.1",
  "co.fs2" %% "fs2-core" % fs2Version,
  "co.fs2" %% "fs2-io" % fs2Version,
  "co.fs2" %% "fs2-reactive-streams" % fs2Version,
  "co.fs2" %% "fs2-experimental" % fs2Version,
  "org.pcap4j" % "pcap4j-packetfactory-static" % "2.0.0-alpha.6" % Test
)

bashScriptExtraDefines += """addJava "-Dlogback.statusListenerClass=ch.qos.logback.core.status.NopStatusListener""""
git.useGitDescribe := true

Compile / packageDoc / publishArtifact := false
packageDoc / publishArtifact := false

def fs2Version = "2.5.3"