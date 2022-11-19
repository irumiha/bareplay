package application

import org.flywaydb.play.FlywayPlayComponents
import play.api.ApplicationLoader.Context
import play.api.db.{DBComponents, Database, HikariCPComponents}
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext, LoggerConfigurator}

class ApplicationComponents(context: Context)
    extends BuiltInComponentsFromContext(context)
    with FlywayPlayComponents
    with DBComponents
    with HikariCPComponents
    with play.filters.HttpFiltersComponents
    with Routes {

  if (Option(System.getProperty("liveReload")).contains("true")) {
    try {
      flywayPlayInitializer
    } catch {
      case e: Exception =>
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

  val database: Database = dbApi.database("default")

  LoggerConfigurator(context.environment.classLoader).foreach {
    _.configure(context.environment, context.initialConfiguration, Map.empty)
  }

}

class Loader extends ApplicationLoader {
  def load(context: Context): Application = new ApplicationComponents(
    context
  ).application
}
