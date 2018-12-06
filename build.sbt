val VERSION = "0.2.1"

val GROUP_ID = "net.petitviolet"

val PROJECT_NAME = "akka-prac"

val AKKA_VERSION = "2.5.18"

lazy val commonDependencies = Seq(
  "com.typesafe.akka" %% "akka-actor" % AKKA_VERSION,
  "com.typesafe.akka" %% "akka-distributed-data" % AKKA_VERSION,
  "com.typesafe.akka" %% "akka-actor-typed" % AKKA_VERSION,

"org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

lazy val commonSettings = Seq(
  version := VERSION,
  organization := GROUP_ID,
  scalaVersion := "2.12.6",
  crossScalaVersions := Seq("2.11.11", "2.12.6"),
  libraryDependencies ++= commonDependencies
)


lazy val root = (project in file("."))
  .settings(commonSettings, name := "root")
  .aggregate(prac)

lazy val prac = (project in file("modules/prac"))
  .settings(commonSettings, name := "prac")
  .settings(scalacOptions += "-Xlog-implicits")

