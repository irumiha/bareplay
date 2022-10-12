ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version      := "1.0-SNAPSHOT"

val playVersion = "2.8.17"
val playComponents = Seq(
  "play",
  "play-akka-http-server",
  "play-ws",
  "play-logback",
  "filters-helpers"
).map("com.typesafe.play" %% _ % playVersion)

lazy val root = (project in file("."))
  .enablePlugins(JavaServerAppPackaging, DockerPlugin)
  .settings(
    name                := """bareplay""",
    fork                := true,
    Compile / mainClass := Some("play.core.server.ProdServerStart"),
    libraryDependencies ++=
      playComponents ++
        Seq(
          "com.typesafe.play"        %% "play-json" % "2.9.3",
          "com.lihaoyi"              %% "scalatags" % "0.12.0",
          "com.softwaremill.macwire" %% "macros"    % "2.5.8" % "provided",
          "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
        ),
    Test / javaOptions ++= Seq(
      "--add-exports=java.base/sun.security.x509=ALL-UNNAMED",
      "--add-opens=java.base/sun.security.ssl=ALL-UNNAMED"
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    ),
    dockerBaseImage := "azul/zulu-openjdk:19-jre-headless",
    dockerExposedPorts ++= Seq(9000),
  )
