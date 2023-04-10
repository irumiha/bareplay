package pages

import scalatags.Text.all.*
import BaseView.baseView

object HomeViews:
  def index: Tag =
    baseView(
      "Welcome to Play",
      div(
        h1("Welcome to Play!")
      )
    )
