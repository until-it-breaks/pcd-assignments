ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.8.3"

lazy val root = (project in file("."))
  .settings(
    name := "smart-home-alarm-system",
    libraryDependencies ++=Seq(
      "org.apache.pekko" %% "pekko-actor-typed" % "1.6.0",
      "ch.qos.logback" % "logback-classic" % "1.5.32"
    )
  )
