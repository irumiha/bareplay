ThisBuild / scalaVersion := "2.13.10"
ThisBuild / version      := "1.0-SNAPSHOT"
ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)

val playVersion           = "2.8.19"
val testcontainersVersion = "1.17.6"
val playComponents = Seq(
  "play",
  "play-akka-http-server",
  "play-ahc-ws",
  "play-logback",
  "play-caffeine-cache",
  "filters-helpers",
  "play-jdbc"
).map("com.typesafe.play" %% _ % playVersion)

lazy val slick = taskKey[Seq[File]]("Generate Tables.scala")

lazy val devcontainers = (project in file("devcontainers"))
  .settings(
    libraryDependencies ++= Seq(
      "org.testcontainers" % "testcontainers"          % testcontainersVersion,
      "org.testcontainers" % "postgresql"              % testcontainersVersion,
      "com.github.dasniko" % "testcontainers-keycloak" % "2.4.0"
    )
  )

lazy val root = (project in file("."))
  .enablePlugins(JavaServerAppPackaging, DockerPlugin)
  .dependsOn(devcontainers)
  .settings(
    name                := """bareplay""",
    fork                := true,
    Compile / mainClass := Some("play.core.server.ProdServerStart"),
    libraryDependencies ++=
      playComponents ++
        Seq(
          "com.typesafe.play"        %% "play-json"          % "2.9.4",
          "org.playframework.anorm"  %% "anorm"              % "2.7.0",
          "com.lihaoyi"              %% "scalatags"          % "0.12.0",
          "org.postgresql"            % "postgresql"         % "42.5.4",
          "com.h2database"            % "h2"                 % "2.1.214",
          "ch.qos.logback"            % "logback-classic"    % "1.4.6",
          "org.flywaydb"             %% "flyway-play"        % "7.37.0",
          "com.github.jwt-scala"     %% "jwt-play-json"      % "9.2.0",
          "com.softwaremill.macwire" %% "macros"             % "2.5.8" % "provided",
          "com.softwaremill.common"  %% "tagging"            % "2.3.4",
          "org.scalatestplus.play"   %% "scalatestplus-play" % "5.1.0" % Test
        ),
    // testcontainers-keycloak brings latest Jackson. Force jackson versions to the ones supported by Play.
    dependencyOverrides ++= Seq(
      "com.fasterxml.jackson.core"   % "jackson-core"                    % "2.11.4",
      "com.fasterxml.jackson.core"   % "jackson-annotations"             % "2.11.4",
      "com.fasterxml.jackson.core"   % "jackson-databind"                % "2.11.4",
      "com.fasterxml.jackson.jaxrs"  % "jackson-jaxrs-base"              % "2.11.4",
      "com.fasterxml.jackson.jaxrs"  % "jackson-jaxrs-json-provider"     % "2.11.4",
      "com.fasterxml.jackson.module" % "jackson-module-jaxb-annotations" % "2.11.4"
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
    addCommandAlias(
      "devReload",
      "~ reStart --- -Dconfig.resource=application.conf -DliveReload=true"
    )
  )
