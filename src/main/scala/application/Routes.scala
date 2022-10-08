package application

import application.modules.AllControllersModule
import play.api.ApplicationLoader.Context
import play.api.BuiltInComponentsFromContext
import controllers.AssetsComponents
import play.api.i18n.I18nComponents
import play.api.routing.Router


abstract class Routes(context: Context)
  extends BuiltInComponentsFromContext(context) // We need this here to be able to inject controllers with ControllerComponents
    with AllControllersModule // Controllers module
    with AssetsComponents // Assets controller, serves static assets
    with I18nComponents { // Languages and internationalization

  val router: Router = {
    import play.api.routing.sird._ // Localize the wildcard import to where the SIRD definitions are needed

    Router.from {
      case GET(p"/") => homeController.index()

      case GET(p"/assets/$file*") =>
        assets.versioned(path = "/public", file = file)
    }
  }

}
