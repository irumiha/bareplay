package application.security

import play.api.mvc.Action
import play.api.mvc.Results.Forbidden

import scala.concurrent.Future

case class SecurityActionWrapper(
    userAuthBuilder: UserAuthenticatedBuilder
) extends play.api.Logging {

  def apply[A](action: Action[A]): Action[A] =
    userAuthBuilder.async(action.parser) { request =>
      action(request)
    }

  def apply[A](roles: Set[String])(action: Action[A]): Action[A] =
    userAuthBuilder.async(action.parser) { request =>
      val matchingRoles = request.user.roles intersect roles
      if (matchingRoles.equals(roles)) {
        action(request)
      } else {
        logger.warn(s"Disallowing access for ${request.path}, roles mismatch")
        Future.successful(Forbidden)
      }
    }
}
