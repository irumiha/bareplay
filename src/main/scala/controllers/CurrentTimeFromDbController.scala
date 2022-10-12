package controllers

import application.{DatabaseExecutionContext, ScalaTagsSupport}
import play.api.db.Database
import play.api.mvc._
import services.DbTimeService
import views.TimeView

import scala.concurrent.Future

class CurrentTimeFromDbController(
    db: Database,
    databaseExecutionContext: DatabaseExecutionContext,
    override val controllerComponents: ControllerComponents
) extends AbstractController(controllerComponents)
    with ScalaTagsSupport {

  def timeFromDb: Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      Future {
        val (currentTime, numInvocations) = db.withConnection { implicit conn =>
          val currTimeFromDb = DbTimeService.getCurrentTimeFromDb
          val callCounter    = DbTimeService.incAndGetCounter

          (currTimeFromDb, callCounter)
        }

        Ok(TimeView.theTime(currentTime, numInvocations))
      }(databaseExecutionContext)
  }
}
