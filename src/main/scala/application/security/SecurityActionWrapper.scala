package application.security

import play.api.mvc.Security.AuthenticatedRequest
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Result}

import scala.concurrent.Future

class SecurityActionWrapper(
    cc: ControllerComponents,
    userAuthBuilder: UserAuthenticatedBuilder
) extends AbstractController(cc)
    with play.api.Logging {

  def action(action: Action[AnyContent]): Action[AnyContent] =
    userAuthBuilder.async { request =>
      action(request)
    }

  def actionWithRoles(roles: Set[String])(action: Action[AnyContent]): Action[AnyContent] =
    userAuthBuilder.async { request =>
      val matchingRoles = request.user.roles intersect roles
      if (matchingRoles.equals(roles)) {
        action(request)
      } else {
        logger.warn(s"Disallowing access for ${request.path}, roles mismatch")
        Future.successful(Forbidden)
      }
    }

  def withRoles(
      roles: Set[String]
  )(block: AuthenticatedRequest[AnyContent, Authentication] => Result): Action[AnyContent] =
    userAuthBuilder { request =>
      val matchingRoles = request.user.roles intersect roles
      if (matchingRoles.equals(roles)) {
        block(request)
      } else {
        Forbidden
      }
    }

  def withRolesAsync(
      roles: Set[String]
  )(block: AuthenticatedRequest[AnyContent, Authentication] => Future[Result]): Action[AnyContent] =
    userAuthBuilder.async { request =>
      val matchingRoles = request.user.roles intersect roles
      if (matchingRoles.equals(roles)) {
        block(request)
      } else {
        Future.successful(Forbidden)
      }
    }

}
