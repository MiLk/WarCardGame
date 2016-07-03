name := """WarCardGame"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.7"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.4.7"
libraryDependencies += "org.scalactic" %% "scalactic" % "2.2.6"
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % "test"
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.12.1" % "test"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
