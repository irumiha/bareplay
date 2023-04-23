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

  def indexSecured(username: String, roles: Set[String]): Tag =
    baseView(
      "Welcome to Play",
      div(
        h1("Welcome to Play!"),
        p(s"Logged in user is: ${username}, roles: ${roles}")
      )
    )
