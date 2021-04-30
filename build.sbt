val scala3Version = "3.0.0-RC3"

lazy val root = project
  .in(file("."))
  .settings(
    name := "statefun-scala3",
    version := "0.1.0",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "org.apache.flink" % "statefun-sdk-java" % "3.0.0",
      "io.circe" %% "circe-core" % "0.14.0-M6",
      "io.circe" %% "circe-parser" % "0.14.0-M6",
      "io.circe" %% "circe-generic" % "0.14.0-M6",
      "io.undertow" % "undertow-core" % "2.2.7.Final",
      "dev.zio" %% "zio" % "1.0.7",
      "org.apache.kafka" % "kafka-clients" % "2.8.0",
      // Testing
      "org.testcontainers" % "testcontainers" % "1.15.3" % Test,
      "org.testcontainers" % "kafka" % "1.15.3" % Test,
      "org.scalameta" %% "munit" % "0.7.25" % Test
    )
  )

enablePlugins(DockerComposePlugin)