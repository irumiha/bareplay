package controllers

import application.security.{RolesSecuredBlocks, UserAuthenticatedBuilder}
import pages.HomeViews
import play.api.i18n.Langs
import play.api.mvc.*

class HomeController(
    langs: Langs,
    cc: ControllerComponents,
    secured: UserAuthenticatedBuilder
) extends AbstractController(cc)
    with RolesSecuredBlocks:
  import application.ScalaTagsSupport.*

  def indexSecured: Action[AnyContent] = secured {
    withRoles(Set("ADMIN")) { request =>
      Ok(HomeViews.index)
    }
  }

  def indexOpen: Action[AnyContent] = Action { request =>
    Ok(HomeViews.index)
  }
