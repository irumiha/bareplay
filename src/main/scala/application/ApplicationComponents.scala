package application

import _root_.controllers.AssetsComponents
import play.api.ApplicationLoader.Context
import play.api._
import play.api.i18n._
import play.api.routing.Router
import play.api.routing.sird._

class ApplicationComponents(context: Context) extends BuiltInComponentsFromContext(context)
  with MainApplicationModule
  with AssetsComponents
  with I18nComponents
  with play.filters.HttpFiltersComponents {

  LoggerConfigurator(context.environment.classLoader).foreach {
    _.configure(context.environment, context.initialConfiguration, Map.empty)
  }

  val router: Router = Router.from {
    case GET(p"/") => homeController.index()
    case GET(p"/assets/$file*") =>
      assets.versioned(path = "/public", file = file)
  }
}

class AppLoader extends ApplicationLoader {
  def load(context: Context): Application = new ApplicationComponents(context).application
}