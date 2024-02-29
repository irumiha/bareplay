import com.typesafe.sbt.packager.docker._

ThisBuild / scalaVersion := "3.4.0"
ThisBuild / version      := "1.0-SNAPSHOT"
ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)

val playVersion           = "3.0.1"
val testcontainersVersion = "1.19.6"
val playComponents = Seq(
  "play",
  "play-pekko-http-server",
  "play-filters-helpers",
  "play-ahc-ws",
  "play-logback",
  "play-caffeine-cache",
  "play-jdbc"
).map("org.playframework" %% _ % playVersion)

lazy val devcontainers = (project in file("devcontainers"))
  .settings(
    libraryDependencies ++= Seq(
      "org.testcontainers" % "testcontainers"          % testcontainersVersion,
      "org.testcontainers" % "postgresql"              % testcontainersVersion,
      "com.github.dasniko" % "testcontainers-keycloak" % "3.2.0"
    )
  )

lazy val root = (project in file("."))
  .enablePlugins(JavaServerAppPackaging, DockerPlugin, AshScriptPlugin)
  .dependsOn(devcontainers)
  .settings(
    name                := """bareplay""",
    fork                := true,
    Compile / mainClass := Some("play.core.server.ProdServerStart"),
    libraryDependencies ++=
      playComponents ++
        Seq(
          "com.typesafe.play"        %% "play-json"          % "2.10.4",
          "com.github.jwt-scala"     %% "jwt-json4s-native"  % "10.0.0",
          "org.playframework.anorm"  %% "anorm"              % "2.7.0",
          "com.lihaoyi"              %% "scalatags"          % "0.12.0",
          "org.flywaydb"              % "flyway-core"        % "9.19.4",
          "org.postgresql"            % "postgresql"         % "42.7.2",
          "com.h2database"            % "h2"                 % "2.2.224",
          "ch.qos.logback"            % "logback-classic"    % "1.5.0",
          "com.softwaremill.macwire" %% "macros"             % "2.5.9"    % "provided",
          "com.softwaremill.common"  %% "tagging"            % "2.3.4",
          "org.scalatestplus.play"   %% "scalatestplus-play" % "7.0.1" % Test
        ),
    Test / javaOptions ++= Seq(
      "--add-exports=java.base/sun.security.x509=ALL-UNNAMED",
      "--add-opens=java.base/sun.security.ssl=ALL-UNNAMED"
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings",
      "-Wunused:imports,privates,locals",
    ),
    dockerBaseImage := "azul/zulu-openjdk-alpine:21",
    dockerExposedPorts ++= Seq(9000),
    dockerCommands := dockerCommands.value.flatMap {
      case DockerStageBreak => Seq(
        ExecCmd("RUN", "apk", "add", "--no-cache", "binutils"),
        ExecCmd(
          "RUN",
          "jlink",
          "--add-modules",
          "ALL-MODULE-PATH",
          "--strip-debug",
          "--no-man-pages",
          "--no-header-files",
          "--compress=2",
          "--output",
          "/jre"
        ),
        DockerStageBreak,
        Cmd("FROM", "alpine:latest"),
        Cmd("ENV", "JAVA_HOME=/jre"),
        Cmd("ENV", """PATH="${JAVA_HOME}/bin:${PATH}""""),
        Cmd("COPY", "--from=stage0", "/jre", """$JAVA_HOME"""),
      )
      case Cmd("FROM", args @_ *) if args.last == "mainstage" => Seq()
      case ExecCmd("ENTRYPOINT", args @_ *) => Seq(
        ExecCmd("ENTRYPOINT", args :+ "-Dconfig.resource=application-prod.conf" :_*)
      )
      case cmd => Seq(cmd)
    },
  )
