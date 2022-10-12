package application.modules

import akka.actor.ActorSystem
import application.{DatabaseExecutionContext, DatabaseExecutionContextImpl}
import controllers.{CurrentTimeFromDbController, HomeController}
import play.api.db.Database
import play.api.i18n.Langs
import play.api.mvc.ControllerComponents

trait AllControllersModule {
  import com.softwaremill.macwire._

  lazy val databaseExecutionContext: DatabaseExecutionContext =
    wire[DatabaseExecutionContextImpl]

  lazy val homeController: HomeController = wire[HomeController]
  lazy val currentTimeFromDbController: CurrentTimeFromDbController =
    wire[CurrentTimeFromDbController]

  def database: Database

  def langs: Langs

  def controllerComponents: ControllerComponents

  def actorSystem: ActorSystem

}
