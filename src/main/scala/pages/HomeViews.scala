package pages

import scalatags.Text.all._
import BaseView.baseView

object HomeViews {
  def index: Tag =
    baseView(
      "Welcome to Play",
      div(
        h1("Welcome to Play!")
      )
    )
}
