package application.modules

import actors.CounterActor
import org.apache.pekko.actor.{ActorRef, ActorSystem, Props}
import application.security.*
import application.{DatabaseExecutionContext, DatabaseExecutionContextImpl}
import controllers.{HomeController, VisitCounterController}
import models.AccessCounterRepository
import play.api.Configuration
import play.api.cache.AsyncCacheApi
import play.api.db.Database
import play.api.i18n.Langs
import play.api.libs.ws.WSClient
import play.api.mvc.ControllerComponents

import java.time.Clock
import scala.concurrent.ExecutionContext

trait AllControllersAndServicesModule:

  import com.softwaremill.macwire.*
  import com.softwaremill.tagging.*

  // Module dependencies
  implicit lazy val executionContext: ExecutionContext
  def cacheApiBuilder(str: String): AsyncCacheApi
  def actorSystem: ActorSystem
  def langs: Langs
  def controllerComponents: ControllerComponents
  def database: Database
  def configuration: Configuration
  def wsClient: WSClient
  def clock: Clock

  // Module components
  lazy val oauth2OidcConfiguration: Oauth2OidcConfiguration = wireWith(
    Oauth2OidcConfiguration.build
  )

  lazy val databaseExecutionContext: DatabaseExecutionContext =
    wire[DatabaseExecutionContextImpl]

  lazy val realmInfoService: RealmInfoService             = wire[RealmInfoService]
  lazy val keycloakTokens: KeycloakTokens                 = wire[KeycloakTokens]
  lazy val homeController: HomeController                 = wire[HomeController]
  lazy val visitCounterController: VisitCounterController = wire[VisitCounterController]
  lazy val securityController: SecurityController         = wire[SecurityController]
  lazy val accessCounterRepository: AccessCounterRepository =
    wire[AccessCounterRepository]

  lazy val counterActorRef: ActorRef @@ CounterActor.Tag = actorSystem
    .actorOf(Props(wire[CounterActor]), "counter-actor")
    .taggedWith[CounterActor.Tag]

  lazy val sessionCache: AuthenticationCache = AuthenticationCache(
    cacheApiBuilder("authentication")
  )

  lazy val userAuthenticatedBuilder: UserAuthenticatedBuilder =
    wire[UserAuthenticatedBuilder]

  lazy val securedAction: SecurityActionWrapper = wire[SecurityActionWrapper]
