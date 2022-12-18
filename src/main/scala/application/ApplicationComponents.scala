package application

import org.flywaydb.play.FlywayPlayComponents
import play.api.ApplicationLoader.Context
import play.api.db.{DBComponents, Database, HikariCPComponents}
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext, LoggerConfigurator}

class ApplicationComponents(context: Context)
    extends BuiltInComponentsFromContext(context)
    with FlywayPlayComponents
    with DBComponents
    with HikariCPComponents
    with play.filters.HttpFiltersComponents
    with AhcWSComponents
    with Routes {

  initializeMigrations

  val database: Database = dbApi.database("default")

  LoggerConfigurator(context.environment.classLoader).foreach {
    _.configure(context.environment, context.initialConfiguration, Map.empty)
  }

  private def initializeMigrations = {
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

class Loader extends ApplicationLoader {
  def load(context: Context): Application = new ApplicationComponents(
    context
  ).application
}
