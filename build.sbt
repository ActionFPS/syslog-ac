scalaVersion := "2.12.8"
scalacOptions := Seq(
  "-unchecked", "-deprecation", "-encoding", "utf8", "-feature",
  "-language:existentials", "-language:implicitConversions",
  "-language:reflectiveCalls", "-target:jvm-1.8"
)
javaOptions += "-Duser.timezone=UTC"
javacOptions in compile in ThisBuild ++= Seq("--release",
  "11",
  "-target",
  "11")

enablePlugins(JavaAppPackaging)
enablePlugins(RpmPlugin)
enablePlugins(GitVersioning)
rpmVendor := "actionfps"
rpmBrpJavaRepackJars := true
rpmLicense := Some("BSD")
libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.8",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "org.pcap4j" % "pcap4j-sample" % "2.0.0-alpha.2" % Test
)
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"
bashScriptExtraDefines += """addJava "-Dlogback.statusListenerClass=ch.qos.logback.core.status.NopStatusListener""""
git.useGitDescribe := true
libraryDependencies += "javax.xml.bind" % "jaxb-api" % "2.3.1"
libraryDependencies += "co.fs2" %% "fs2-core" % "1.0.4"
libraryDependencies += "co.fs2" %% "fs2-io" % "1.0.4"
libraryDependencies += "co.fs2" %% "fs2-reactive-streams" % "1.0.4"
libraryDependencies += "co.fs2" %% "fs2-experimental" % "1.0.4"
