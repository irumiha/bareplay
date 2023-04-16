package application

import org.flywaydb.core.Flyway
import play.api.{Configuration, Logging}

trait MigrationsSupport extends Logging:
  protected def initializeMigrations(config: Configuration): Unit =
    if config.getOptional[Boolean]("db.migrations.devmode").getOrElse(false) then
      try
        val flyway = flywayBaseConfiguration(config)
          .cleanDisabled(false)
          .cleanOnValidationError(true)
          .load()
        flyway.migrate()
      catch
        case e: Exception =>
          logger.error("Migrations failed: ", e)
          if !config.getOptional[Boolean]("db.migrations.lenient").getOrElse(false) then
            throw e
    else
      val flyway = flywayBaseConfiguration(config).load()
      flyway.migrate()

  private def flywayBaseConfiguration(config: Configuration) =
    Flyway
      .configure()
      .dataSource(
        config.get[String]("db.default.url"),
        config.get[String]("db.default.username"),
        config.get[String]("db.default.password")
      )
