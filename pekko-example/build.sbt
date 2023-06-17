name := "pekko-example"

version := "1.0"

scalaVersion := "2.13.10"

lazy val pekkoVersion = "0.0.0+26648-878ee613-SNAPSHOT"

// Run in a separate JVM, to make sure sbt waits until all threads have
// finished before returning.
// If you want to keep the application running while executing other
// sbt tasks, consider https://github.com/spray/sbt-revolver/
fork := true

resolvers += "Apache Nexus Snapshots".at("https://repository.apache.org/content/repositories/snapshots/")

libraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-actor-typed" % pekkoVersion,
  "org.apache.pekko" %% "pekko-persistence" % pekkoVersion,
  "ch.qos.logback" % "logback-classic" % "1.4.7",
  "org.apache.pekko" %% "pekko-actor-testkit-typed" % pekkoVersion % Test,
  "org.scalatest" %% "scalatest" % "3.2.15" % Test
)
