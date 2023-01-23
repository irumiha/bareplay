package application.modules

import akka.actor.{ActorRef, ActorSystem, Props}
import actors.CounterActor
import application.security.{
  Authentication,
  KeycloakTokens,
  Oauth2OidcConfiguration,
  RealmInfoService,
  SecurityActionWrapper,
  SecurityController,
  UserAuthenticatedBuilder
}
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

trait AllControllersAndServicesModule {
  import com.softwaremill.macwire._
  import com.softwaremill.tagging._

  // Module dependencies
  def sessionCache: AsyncCacheApi @@ Authentication
  implicit val executionContext: ExecutionContext
  def actorSystem: ActorSystem
  def langs: Langs
  def controllerComponents: ControllerComponents
  def database: Database
  def configuration: Configuration
  def wsClient: WSClient
  def clock: Clock

  // Module components
  lazy val oauth2OidcConfiguration: Oauth2OidcConfiguration =
    configuration.get[Oauth2OidcConfiguration]("security.oauth2_oidc")
  lazy val realmInfoService: RealmInfoService =
    new RealmInfoService(oauth2OidcConfiguration, wsClient)
  lazy val keycloakTokens: KeycloakTokens                 = wire[KeycloakTokens]

  lazy val homeController: HomeController                 = wire[HomeController]
  lazy val visitCounterController: VisitCounterController = wire[VisitCounterController]
  lazy val securityController: SecurityController         = wire[SecurityController]
  lazy val databaseExecutionContext: DatabaseExecutionContext =
    wire[DatabaseExecutionContextImpl]

  lazy val accessCounterRepository: AccessCounterRepository = wire[AccessCounterRepository]

  lazy val counterActorRef: ActorRef @@ CounterActor.Tag = actorSystem
    .actorOf(Props(wire[CounterActor]), "counter-actor")
    .taggedWith[CounterActor.Tag]

  lazy val userAuthenticatedBuilder: UserAuthenticatedBuilder = wireWith(
    UserAuthenticatedBuilder.build(
      _: ControllerComponents,
      _: Configuration,
      _: RealmInfoService,
      _: AsyncCacheApi @@ Authentication,
      _: KeycloakTokens,
      _: Clock
    )
  )
  lazy val securedAction: SecurityActionWrapper = wire[SecurityActionWrapper]

}
