package application

import devcontainers.AllAppContainers
import org.testcontainers.containers.GenericContainer
import play.api.ApplicationLoader.Context
import play.api.cache.AsyncCacheApi
import play.api.cache.caffeine.CaffeineCacheComponents
import play.api.db.{DBComponents, Database, HikariCPComponents}
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.*
import play.filters.HttpFiltersComponents
import java.time.Clock

trait ApplicationComponents
    extends BuiltInComponents
    with DBComponents
    with HikariCPComponents
    with HttpFiltersComponents
    with AhcWSComponents
    with CaffeineCacheComponents
    with MigrationsSupport
    with Routes:

  lazy val database: Database = dbApi.database("default")
  lazy val clock: Clock       = Clock.systemUTC()
  override def cacheApiBuilder(cacheName: String): AsyncCacheApi = cacheApi(cacheName)

  initializeMigrations(configuration)

class Loader extends ApplicationLoader:
  def load(context: Context): Application =
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment, context.initialConfiguration, Map.empty)
    }
    (new BuiltInComponentsFromContext(context) with ApplicationComponents).application

class DevLoader extends ApplicationLoader with AllAppContainers:

  override def realmName: String = "dev-realm"

  override def containers: Seq[GenericContainer[?]] =
    Seq(postgresContainer.withReuse(true), keycloakContainer.withReuse(true))

  override def load(context: Context): Application =
    containers.foreach(c => c.start())

    val configurationFromContainers = Configuration.from(containerConfiguration)

    val devContext = context.copy(initialConfiguration =
      configurationFromContainers.withFallback(context.initialConfiguration)
    )

    (new BuiltInComponentsFromContext(devContext) with ApplicationComponents).application
