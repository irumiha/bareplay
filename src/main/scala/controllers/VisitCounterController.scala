package controllers

import org.apache.pekko.actor.*
import org.apache.pekko.pattern.ask
import org.apache.pekko.util.Timeout
import actors.CounterActor
import com.softwaremill.tagging.@@
import models.AccessCounterRow
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

case class VisitCounterResponse(count: Long)

object VisitCounterResponse:
  implicit def jsonSerialize: OFormat[VisitCounterResponse] =
    Json.format[VisitCounterResponse]

class VisitCounterController(
    cc: ControllerComponents,
    counterActor: ActorRef @@ CounterActor.Tag
) extends AbstractController(cc):
  def visitCount: Action[AnyContent] = Action.async { implicit request =>
    implicit val timeout: Timeout = 7.seconds

    (counterActor ? CounterActor.IncrementCounter(1)).mapTo[AccessCounterRow].map { c =>
      Ok(Json.toJson(VisitCounterResponse(c.counter)))
    }
  }
