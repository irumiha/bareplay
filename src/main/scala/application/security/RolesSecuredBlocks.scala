package application.security

import play.api.mvc.{AnyContent, Result}
import play.api.mvc.Results.Forbidden
import play.api.mvc.Security.AuthenticatedRequest

import scala.concurrent.Future

/** Both methods here wrap an existing action block by first checking for existence of a valid JWT
  * in a cookie. If the JWT is valid then check the roles given in the "realm_access" \ "roles"
  * claim.
  *
  * ALL REQUESTED ROLES MUST BE PRESENT IN JWT
  */
trait RolesSecuredBlocks extends play.api.Logging {
  // TODO return proper UI error page for Forbidden case
  type AuthenticatedBlock[A] = AuthenticatedRequest[A, Authentication] => Result
  def withRoles(
      roles: Set[String]
  )(block: AuthenticatedBlock[AnyContent]): AuthenticatedBlock[AnyContent] = { request =>
    val matchingRoles = request.user.roles intersect roles
    if (matchingRoles.equals(roles)) {
      block(request)
    } else {
      logger.warn(s"Roles mismatch, received: ${request.user.roles}, required: $roles")
      Forbidden
    }
  }

  type AuthenticatedAsyncBlock[A] = AuthenticatedRequest[A, Authentication] => Future[Result]
  def withRolesAsync[A](
      roles: Set[String]
  )(block: AuthenticatedAsyncBlock[AnyContent]): AuthenticatedAsyncBlock[AnyContent] = { request =>
    val matchingRoles = request.user.roles intersect roles
    if (matchingRoles.equals(roles)) {
      block(request)
    } else {
      logger.warn(s"Roles mismatch, received: ${request.user.roles}, required: $roles")
      Future.successful(Forbidden)
    }
  }
}
