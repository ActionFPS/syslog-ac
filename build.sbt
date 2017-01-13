scalaVersion := "2.12.1"
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
  "org.joda" % "joda-convert" % "1.8.1"
)
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"
bashScriptExtraDefines += """addJava "-Dlogback.statusListenerClass=ch.qos.logback.core.status.NopStatusListener""""
git.useGitDescribe := true
fork := true
