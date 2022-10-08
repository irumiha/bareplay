package application.modules

import controllers.HomeController
import play.api.i18n.Langs
import play.api.mvc.ControllerComponents

trait AllControllersModule {

  import com.softwaremill.macwire._

  lazy val homeController: HomeController = wire[HomeController]

  def langs: Langs

  def controllerComponents: ControllerComponents
}
