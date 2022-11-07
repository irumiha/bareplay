addSbtPlugin("com.github.sbt"        % "sbt-native-packager" % "1.9.11")
addSbtPlugin("io.spray"              % "sbt-revolver"        % "0.9.1")
addSbtPlugin("org.scalameta"         % "sbt-scalafmt"        % "2.4.6")
addSbtPlugin("io.github.davidmweber" % "flyway-sbt"          % "7.4.0")

libraryDependencies += "org.postgresql"       % "postgresql"         % "42.5.0"