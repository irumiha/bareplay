package controllers

import play.api.i18n.Langs
import play.api.mvc._
import views.HomeViews

class HomeController(
    langs: Langs,
    override val controllerComponents: ControllerComponents
) extends AbstractController(controllerComponents) {
  import application.ScalaTagsSupport._

  def index: Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      Ok(HomeViews.index)
  }
}
