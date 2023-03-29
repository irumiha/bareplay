package application

import akka.Done
import akka.actor.CoordinatedShutdown
import application.security.Authentication

import scala.concurrent.ExecutionContext.Implicits.global
import com.softwaremill.tagging.{@@, Tagger}
import devcontainers.AllAppContainers
import org.testcontainers.containers.GenericContainer
import play.api.ApplicationLoader.Context
import play.api.cache.AsyncCacheApi
import play.api.cache.caffeine.CaffeineCacheComponents
import play.api.db.{DBComponents, Database, HikariCPComponents}
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext, Configuration, LoggerConfigurator}

import java.time.Clock
import scala.concurrent.Future

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

class DevLoader extends ApplicationLoader with AllAppContainers {

  override def realmName: String = "dev-realm"

  override def containers: Seq[GenericContainer[_]] =
    Seq(postgresContainer.withReuse(true), keycloakContainer.withReuse(true))

  override def load(context: Context): Application = {
    containers.foreach(c => c.start())

    val configurationFromContainers = Configuration.from(containerConfiguration)

    val devContext = context.copy(initialConfiguration =
      configurationFromContainers.withFallback(context.initialConfiguration)
    )

    new ApplicationComponents(
      devContext
    ).application
  }
}