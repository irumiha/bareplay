package application.security

import play.api.mvc.{AnyContent, Result}
import play.api.mvc.Results.Forbidden
import play.api.mvc.Security.AuthenticatedRequest

import scala.concurrent.Future

trait RolesSecuredBlocks {
  type AuthenticatedBlock[A] = AuthenticatedRequest[A, Authentication] => Result
  def withRoles(
    roles: Set[String]
  )(block: AuthenticatedBlock[AnyContent]): AuthenticatedBlock[AnyContent] = { request =>
    val matchingRoles = request.user.roles intersect roles
    if (matchingRoles.equals(roles)) {
      block(request)
    } else {
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
      Future.successful(Forbidden)
    }
  }
}
