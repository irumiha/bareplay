package application

import application.modules.AllControllersAndServicesModule
import controllers.AssetsComponents
import play.api.i18n.I18nComponents
import play.api.routing.Router
import play.api.routing.sird._

trait Routes
    extends AllControllersAndServicesModule // Our controllers module
    with AssetsComponents // Assets controller, serves static assets
    with I18nComponents { // Languages and internationalization

  val router: Router =
    Router.from {
      case GET(p"/")        => homeController.index
      case GET(p"/counter") => visitCounterController.visitCount

      case GET(p"/security/oauth/callback") => securityController.oauthCallback

      case GET(p"/assets/$file*") =>
        assets.versioned(path = "/public", file = file)
    }

}
