package controllers

import play.api.i18n.Langs
import play.api.mvc._

class HomeController(
    langs: Langs,
    override val controllerComponents: ControllerComponents
) extends AbstractController(controllerComponents) {

  def index(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      Ok("Welcome to Play!").as("text/html")
  }
}
