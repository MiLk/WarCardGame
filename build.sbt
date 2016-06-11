name := "WarCardGame"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4-SNAPSHOT"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.4-SNAPSHOT"
libraryDependencies += "org.scalactic" %% "scalactic" % "2.2.6"
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % "test"
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.12.1" % "test"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
