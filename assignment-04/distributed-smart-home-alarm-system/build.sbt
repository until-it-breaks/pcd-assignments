val PekkoVersion = "1.6.0"

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.8.3"

lazy val root = (project in file("."))
  .settings(
    name := "distributed-smart-home-alarm-system",
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-actor-typed" % PekkoVersion,
      "org.apache.pekko" %% "pekko-actor-testkit-typed" % PekkoVersion % Test,
      "org.scalatest" %% "scalatest" % "3.2.20" % Test,
      "ch.qos.logback" % "logback-classic" % "1.5.32",
      "org.apache.pekko" %% "pekko-cluster-typed" % PekkoVersion,
      "org.apache.pekko" %% "pekko-serialization-jackson" % PekkoVersion
    )
  )
