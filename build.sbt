scalaVersion := "2.12.8"
scalacOptions := Seq(
  "-unchecked", "-deprecation", "-encoding", "utf8", "-feature",
  "-language:existentials", "-language:implicitConversions",
  "-language:reflectiveCalls", "-target:jvm-1.8"
)
javaOptions += "-Duser.timezone=UTC"
javaOptions in run += "-Duser.timezone=UTC"
enablePlugins(JavaAppPackaging)
enablePlugins(RpmPlugin)
enablePlugins(GitVersioning)
rpmVendor := "actionfps"
rpmBrpJavaRepackJars := true
rpmLicense := Some("BSD")
libraryDependencies ++= Seq(
  "org.syslog4j" % "syslog4j" % "0.9.30",
  "ch.qos.logback" % "logback-classic" % "1.1.8",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "joda-time" % "joda-time" % "2.9.6",
  "org.joda" % "joda-convert" % "1.8.1",
  "org.pcap4j" % "pcap4j-sample" % "2.0.0-alpha.2" % Test
)
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"
bashScriptExtraDefines += """addJava "-Dlogback.statusListenerClass=ch.qos.logback.core.status.NopStatusListener""""
git.useGitDescribe := true

// https://mvnrepository.com/artifact/javax.xml.bind/jaxb-api
libraryDependencies += "javax.xml.bind" % "jaxb-api" % "2.3.1"

// available for Scala 2.11, 2.12
libraryDependencies += "co.fs2" %% "fs2-core" % "1.0.4"

// optional I/O library
libraryDependencies += "co.fs2" %% "fs2-io" % "1.0.4"

// optional reactive streams interop
libraryDependencies += "co.fs2" %% "fs2-reactive-streams" % "1.0.4"

// optional experimental library
libraryDependencies += "co.fs2" %% "fs2-experimental" % "1.0.4"
