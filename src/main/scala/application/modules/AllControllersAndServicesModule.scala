package application.modules

import akka.actor.ActorSystem
import application.security.{SecurityActionWrapper, SecurityController, UserAuthenticatedBuilder}
import application.{DatabaseExecutionContext, DatabaseExecutionContextImpl}
import controllers.{HomeController, VisitCounterController}
import models.AccessCounterRepository
import play.api.Configuration
import play.api.db.Database
import play.api.i18n.Langs
import play.api.libs.ws.WSClient
import play.api.mvc.{AnyContent, BodyParser, ControllerComponents}

trait AllControllersAndServicesModule {
  import com.softwaremill.macwire._

  lazy val homeController: HomeController = wire[HomeController]
  lazy val visitCounterController: VisitCounterController = wire[VisitCounterController]
  lazy val securityController: SecurityController = wire[SecurityController]
  lazy val databaseExecutionContext: DatabaseExecutionContext =
    wire[DatabaseExecutionContextImpl]

  lazy val accessCounterRepository: AccessCounterRepository = wire[AccessCounterRepository]

  lazy val userAuthenticatedBuilder: UserAuthenticatedBuilder = wire[UserAuthenticatedBuilder]
  lazy val securedAction: SecurityActionWrapper = wire[SecurityActionWrapper]

  def actorSystem: ActorSystem
  def langs: Langs
  def controllerComponents: ControllerComponents
  def database: Database
  def configuration: Configuration
  def wsClient: WSClient
}
