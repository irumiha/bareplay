package controllers

import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import models.{AccessCounterRepository, AccessCounterRow}
import play.api.libs.json.{Json, OFormat}

import scala.concurrent.ExecutionContext.Implicits.global

case class VisitCounterResponse(count: Long)

object VisitCounterResponse {
  implicit def jsonSerialize: OFormat[VisitCounterResponse] = Json.format[VisitCounterResponse]
}

class VisitCounterController(
    cc: ControllerComponents,
    accessCounterRepository: AccessCounterRepository
) extends AbstractController(cc) {

  def visitCount: Action[AnyContent] = Action.async { implicit request =>
    accessCounterRepository
      .increment(1)
      .map { maybeCount =>
        maybeCount.getOrElse(0L)
      }
      .map(c => Ok(Json.toJson(VisitCounterResponse(c))))
  }

}
