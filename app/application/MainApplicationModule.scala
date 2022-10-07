package application

import controllers.HomeController
import play.api.i18n.Langs
import play.api.mvc.ControllerComponents

trait MainApplicationModule {

  import com.softwaremill.macwire._

  lazy val homeController: HomeController = wire[HomeController]

  def langs: Langs

  def controllerComponents: ControllerComponents
}
