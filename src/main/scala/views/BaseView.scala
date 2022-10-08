package views

import scalatags.Text.all._

object BaseView {
  def baseView(pageTitle: String, content: Tag): Tag =
    html(
      lang := "en",
      head(
        title := pageTitle,
        link(
          rel   := "stylesheet",
          media := "screen",
          href  := "/assets/stylesheets/main.css"
        ),
        link(
          rel    := "shortcut icon",
          `type` := "image/png",
          href   := "/assets/images/favicon.png"
        )
      ),
      body(
        content,
        script(
          src    := "/assets/javascripts/main.js",
          `type` := "text/javascript"
        )
      )
    )

}
