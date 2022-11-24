package application.security

import play.api.mvc.Action
import play.api.mvc.Results.Forbidden

import scala.concurrent.Future

case class SecurityActionWrapper(
    userAuthBuilder: UserAuthenticatedBuilder
) extends play.api.Logging {

  /**
   * Wraps an existing action by checking for existence of a valid JWT in a cookie
   */
  def apply[A](action: Action[A]): Action[A] =
    userAuthBuilder.async(action.parser) { request =>
      action(request)
    }

  /**
   * Wraps an existing action by checking for existence of a valid JWT in a cookie.
   * If the JWT is valid then check the roles given in the "realm_access" \ "roles" claim.
   *
   * ALL REQUESTED ROLES MUST BE PRESENT IN JWT
   */
  def apply[A](roles: Set[String])(action: Action[A]): Action[A] =
    userAuthBuilder.async(action.parser) { request =>
      val matchingRoles = request.user.roles intersect roles
      if (matchingRoles.equals(roles)) {
        action(request)
      } else {
        // TODO return proper UI error page for Forbidden case
        logger.warn(s"Roles mismatch, received: ${request.user.roles}, required: ${roles}")
        Future.successful(Forbidden)
      }
    }
}
