package application

import application.security.Authentication
import com.softwaremill.tagging.{@@, Tagger}
import play.api.ApplicationLoader.Context
import play.api.cache.AsyncCacheApi
import play.api.cache.caffeine.CaffeineCacheComponents
import play.api.db.{DBComponents, Database, HikariCPComponents}
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext, LoggerConfigurator}

import java.time.Clock

class ApplicationComponents(context: Context)
    extends BuiltInComponentsFromContext(context)
    with DBComponents
    with HikariCPComponents
    with play.filters.HttpFiltersComponents
    with AhcWSComponents
    with MigrationsSupport
    with CaffeineCacheComponents
    with Routes {

  lazy val database: Database = dbApi.database("default")
  lazy val clock: Clock = Clock.systemUTC()
  lazy val sessionCache: AsyncCacheApi @@ Authentication =
    cacheApi("authentication").taggedWith[Authentication]

  LoggerConfigurator(context.environment.classLoader).foreach {
    _.configure(context.environment, context.initialConfiguration, Map.empty)
  }

  initializeMigrations()

}

class Loader extends ApplicationLoader {
  def load(context: Context): Application = new ApplicationComponents(
    context
  ).application
}
