name := "akka-essentials"

version := "0.1"

scalaVersion := "2.13.10"

val akkaVersion = "2.7.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.2.14"
)
