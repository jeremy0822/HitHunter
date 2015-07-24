import _root_.sbt.Keys._

organization  := "com.cubead"

name := "hithunter"

version := "1.0.0"

scalaVersion := "2.11.6"

resolvers += "rediscala" at "http://dl.bintray.com/etaty/maven"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.3.8"
  val sprayV = "1.3.2"
  Seq(
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
    "org.apache.kafka" %% "kafka" % "0.8.2.1",
    "com.etaty.rediscala" %% "rediscala" % "1.4.0",
    "com.ning" % "async-http-client" % "1.9.24",
    "org.json4s" %% "json4s-native" % "3.2.11",
    "com.sclasen" %% "akka-kafka" % "0.1.0" % "compile",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka" %%  "akka-slf4j"      % akkaV,
    "ch.qos.logback"     %  "logback-classic" % "1.0.13",
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test"
  )
}

resolvers += "sbt-pack repo" at " http://repo1.maven.org/maven2/org/xerial/sbt/"

packSettings

packMain := Map(
  "start" -> "com.cubead.Application"
)
