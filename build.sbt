ThisBuild / scalaVersion := "3.3.0-RC3"
ThisBuild / version      := "1.0-SNAPSHOT"
ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)

val playVersion           = "2.9.0-M4"
val testcontainersVersion = "1.17.6"
val playComponents = Seq(
  "play",
  "play-netty-server",
  "play-ahc-ws",
  "play-logback",
  "play-caffeine-cache",
  "filters-helpers",
  "play-jdbc"
).map("com.typesafe.play" %% _ % playVersion)

lazy val devcontainers = (project in file("devcontainers"))
  .settings(
    libraryDependencies ++= Seq(
      "org.testcontainers" % "testcontainers"          % testcontainersVersion,
      "org.testcontainers" % "postgresql"              % testcontainersVersion,
      "com.github.dasniko" % "testcontainers-keycloak" % "2.5.0"
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
          "com.typesafe.play"        %% "play-json"          % "2.10.0-RC7",
          "com.github.jwt-scala"     %% "jwt-json4s-native"  % "9.2.0",
          "org.playframework.anorm"  %% "anorm"              % "2.7.0",
          "com.lihaoyi"              %% "scalatags"          % "0.12.0",
          "org.flywaydb"              % "flyway-core"        % "9.16.3",
          "org.postgresql"            % "postgresql"         % "42.6.0",
          "com.h2database"            % "h2"                 % "2.1.214",
          "ch.qos.logback"            % "logback-classic"    % "1.4.6",
          "com.softwaremill.macwire" %% "macros"             % "2.5.8"    % "provided",
          "com.softwaremill.common"  %% "tagging"            % "2.3.4",
          "org.scalatestplus.play"   %% "scalatestplus-play" % "6.0.0-M3" % Test
        ),
    Test / javaOptions ++= Seq(
      "--add-exports=java.base/sun.security.x509=ALL-UNNAMED",
      "--add-opens=java.base/sun.security.ssl=ALL-UNNAMED"
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings",
      "-Wunused:imports,privates,locals"
    ),
    dockerBaseImage := "azul/zulu-openjdk:19-jre-headless",
    dockerExposedPorts ++= Seq(9000),
    addCommandAlias(
      "devReload",
      "~ reStart --- -Dconfig.resource=application.conf -DliveReload=true"
    )
  )
