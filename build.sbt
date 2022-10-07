ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(JavaServerAppPackaging)
  .settings(
    name := """bareplay""",
    fork := true,
    Compile / mainClass := Some("play.core.server.ProdServerStart"),
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play" % "2.8.17",
      "com.typesafe.play" %% "play-akka-http-server" % "2.8.17",
      "com.typesafe.play" %% "play-json" % "2.9.3",
      "com.typesafe.play" %% "play-ws" % "2.8.17",
      "com.typesafe.play" %% "play-logback" % "2.8.17",
      "com.typesafe.play" %% "filters-helpers" % "2.8.17",
      "com.lihaoyi" %% "scalatags" % "0.12.0",
      "com.softwaremill.macwire" %% "macros" % "2.5.8" % "provided",
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
      )
  )