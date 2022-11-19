package application.modules

import akka.actor.ActorSystem
import application.{DatabaseExecutionContext, DatabaseExecutionContextImpl}
import controllers.{HomeController, VisitCounterController}
import models.AccessCounterRepository
import play.api.db.Database
import play.api.i18n.Langs
import play.api.mvc.ControllerComponents

trait AllControllersAndServicesModule {
  import com.softwaremill.macwire._

  lazy val homeController: HomeController = wire[HomeController]
  lazy val visitCounterController: VisitCounterController = wire[VisitCounterController]
  lazy val databaseExecutionContext: DatabaseExecutionContext =
    wire[DatabaseExecutionContextImpl]

  lazy val accessCounterRepository: AccessCounterRepository = wire[AccessCounterRepository]


  def actorSystem: ActorSystem
  def langs: Langs
  def controllerComponents: ControllerComponents
  def database: Database

}