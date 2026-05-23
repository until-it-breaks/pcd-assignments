val PekkoVersion = "1.6.0"

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.8.3"

lazy val root = (project in file("."))
  .settings(
    name := "clustered-smart-home-alarm-system",
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-actor-typed" % PekkoVersion,
      "org.apache.pekko" %% "pekko-actor-testkit-typed" % PekkoVersion % Test,
      "org.scalatest" %% "scalatest" % "3.2.20" % Test,
      "ch.qos.logback" % "logback-classic" % "1.5.32",
      "org.apache.pekko" %% "pekko-cluster-typed" % PekkoVersion,
      "org.apache.pekko" %% "pekko-serialization-jackson" % PekkoVersion
    )
  )

// Compile the final artifact into a named binary located at target/app.jar
// Dockerfile needs a fixed string to copy the binary into the container image
assembly / assemblyOutputPath := baseDirectory.value / "target" / "app.jar"

// Many modern Java libraries include a special metadata file called module-info.class.
// If different libraries include this file sbt-assembly refuses to build the jar because it doesn't know which version to keep.
// It is a metadata descriptor file designed to tell the JVM which internal packages a library is allowed to export
// to other modular libraries. Our final fat JAR is inherently non-modular, and we don't need it.
assembly / assemblyMergeStrategy := {
  // Discard it if found
  case PathList("META-INF", "versions", "9", "module-info.class") => MergeStrategy.discard
  case PathList("module-info.class")                              => MergeStrategy.discard
  // standard sbt rule which gracefully merges them or appends them sequentially
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}
