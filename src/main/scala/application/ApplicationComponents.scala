package application

import play.api.*
import play.api.cache.AsyncCacheApi
import play.api.cache.caffeine.CaffeineCacheComponents
import play.api.db.{DBComponents, Database, HikariCPComponents}
import play.api.libs.ws.ahc.AhcWSComponents
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
