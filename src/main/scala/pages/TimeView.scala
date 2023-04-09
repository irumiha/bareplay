package pages

import scalatags.Text.all._
import BaseView.baseView

import java.time.LocalDateTime

object TimeView {
  def theTime(timeToShow: LocalDateTime, invocations: Int): Tag =
    baseView(
      "Time is",
      div(
        h1(
          s"The time is ${timeToShow.toString}, this endpoint was called $invocations times"
        )
      )
    )
}
