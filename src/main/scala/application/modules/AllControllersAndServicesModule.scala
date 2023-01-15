package application.modules

import akka.actor.{ActorRef, ActorSystem, Props}
import actors.CounterActor
import application.security.{SecurityActionWrapper, SecurityController, UserAuthenticatedBuilder}
import application.{DatabaseExecutionContext, DatabaseExecutionContextImpl}
import controllers.{HomeController, VisitCounterController}
import models.AccessCounterRepository
import play.api.Configuration
import play.api.db.Database
import play.api.i18n.Langs
import play.api.libs.ws.WSClient
import play.api.mvc.ControllerComponents

trait AllControllersAndServicesModule {
  import com.softwaremill.macwire._
  import com.softwaremill.tagging._

  lazy val homeController: HomeController = wire[HomeController]
  lazy val visitCounterController: VisitCounterController = wire[VisitCounterController]
  lazy val securityController: SecurityController = wire[SecurityController]
  lazy val databaseExecutionContext: DatabaseExecutionContext =
    wire[DatabaseExecutionContextImpl]

  lazy val accessCounterRepository: AccessCounterRepository = wire[AccessCounterRepository]

  lazy val counterActorRef: ActorRef @@ CounterActor.Tag = actorSystem
    .actorOf(Props(wire[CounterActor]), "counter-actor")
    .taggedWith[CounterActor.Tag]

  lazy val userAuthenticatedBuilder: UserAuthenticatedBuilder = wire[UserAuthenticatedBuilder]
  lazy val securedAction: SecurityActionWrapper = wire[SecurityActionWrapper]

  def actorSystem: ActorSystem
  def langs: Langs
  def controllerComponents: ControllerComponents
  def database: Database
  def configuration: Configuration
  def wsClient: WSClient
}
