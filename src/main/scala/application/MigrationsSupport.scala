package application

import org.flywaydb.core.Flyway
import play.api.Configuration

trait MigrationsSupport {
  protected def initializeMigrations(config: Configuration): Unit = {
    if (Option(System.getProperty("liveReload")).contains("true")) {
      try {
        val flyway = flywayBaseConfiguration(config)
          .cleanOnValidationError(true)
          .load()
        flyway.migrate()
      } catch {
        case e: Exception =>
          // Supress exceptions from bubbling up, to keep the application alive.
          e.printStackTrace(System.err)
          System.err.println("""
              |
              |=================
              |
              | Migrations failed, fix them and save again
              |
              |=================
              |
              |""".stripMargin)
      }
    } else {
      val flyway = flywayBaseConfiguration(config).load()
      flyway.migrate()
    }
  }

  private def flywayBaseConfiguration(config: Configuration) = {
    Flyway
      .configure()
      .dataSource(
        config.get[String]("db.default.url"),
        config.get[String]("db.default.username"),
        config.get[String]("db.default.password")
        )
  }
}
