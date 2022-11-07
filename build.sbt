ThisBuild / scalaVersion := "2.13.10"
ThisBuild / version      := "1.0-SNAPSHOT"

val devDbUrl      = "jdbc:postgresql://localhost:5432/localdev"
val devDbUsername = "localdev"
val devDbPassword = "localdev"

val playVersion  = "2.8.17"
val slickVersion = "3.4.1"
val playComponents = Seq(
  "play",
  "play-akka-http-server",
  "play-ws",
  "play-logback",
  "filters-helpers",
  "play-jdbc",
  "play-jdbc-evolutions"
).map("com.typesafe.play" %% _ % playVersion)

lazy val slick = taskKey[Seq[File]]("Generate Tables.scala")

addCommandAlias("build", "; flywayMigrate; slick; compile")

lazy val root = (project in file("."))
  .enablePlugins(JavaServerAppPackaging, DockerPlugin, FlywayPlugin)
  .settings(
    name                := """bareplay""",
    fork                := true,
    Compile / mainClass := Some("play.core.server.ProdServerStart"),
    libraryDependencies ++=
      playComponents ++
        Seq(
          "com.typesafe.play"       %% "play-json"       % "2.9.3",
          "com.typesafe.play"       %% "play-jdbc"       % "2.8.18",
          "org.playframework.anorm" %% "anorm"           % "2.7.0",
          "com.lihaoyi"             %% "scalatags"       % "0.12.0",
          "org.postgresql"           % "postgresql"      % "42.5.0",
          "ch.qos.logback"           % "logback-classic" % "1.4.4",
          "org.flywaydb"            %% "flyway-play"     % "7.25.0",
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
    ),
    dockerBaseImage := "azul/zulu-openjdk:19-jre-headless",
    dockerExposedPorts ++= Seq(9000),
    flywayUrl                    := devDbUrl,
    flywayUser                   := devDbUsername,
    flywayPassword               := devDbPassword,
    flywayLocations              := Seq("classpath:db/migration"),
    flywayCleanOnValidationError := true,
    Test / flywayUrl             := devDbUrl,
    Test / flywayUser            := devDbUsername,
    Test / flywayPassword        := devDbPassword
  )
