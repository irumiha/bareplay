package application

import org.flywaydb.play.FlywayPlayComponents

trait MigrationsSupport extends FlywayPlayComponents {
  protected def initializeMigrations(): Unit = {
    if (Option(System.getProperty("liveReload")).contains("true")) {
      try {
        flywayPlayInitializer
      } catch {
        case e: Exception =>
          // Supress exceptions from bubbling up, to keep the application alive.
          e.printStackTrace(System.err)
          System.err.println(
            """
              |
              |=================
              |
              |Migrations failed, fix them and save again
              |
              |=================
              |
              |""".stripMargin)
      }
    } else {
      flywayPlayInitializer
    }
  }

}
