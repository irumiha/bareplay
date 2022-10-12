package application

import play.api.ApplicationLoader.Context
import play.api.db.evolutions.EvolutionsComponents
import play.api.db.{DBComponents, Database, HikariCPComponents, PooledDatabase}
import play.api.{
  Application,
  ApplicationLoader,
  BuiltInComponentsFromContext,
  LoggerConfigurator
}

class ApplicationComponents(context: Context)
    extends BuiltInComponentsFromContext(
      context
    )
    with DBComponents
    with EvolutionsComponents
    with HikariCPComponents
    with play.filters.HttpFiltersComponents
    with Routes {

  // this will actually run the database migrations on startup
  applicationEvolutions

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
