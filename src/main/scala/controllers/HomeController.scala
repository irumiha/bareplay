package controllers

import application.security.SecurityActionWrapper
import play.api.i18n.Langs
import play.api.mvc._
import views.HomeViews

class HomeController(
    langs: Langs,
    cc: ControllerComponents,
    secured: SecurityActionWrapper
) extends AbstractController(cc) {
  import application.ScalaTagsSupport._

  def indexSecured: Action[AnyContent] = secured.withRoles(Set("ADMIN")) { request =>
    Ok(HomeViews.index)
  }

  def indexOpen: Action[AnyContent] = Action { request =>
    Ok(HomeViews.index)
  }

}
