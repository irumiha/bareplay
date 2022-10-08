package application

import play.api.ApplicationLoader.Context
import play.api.{LoggerConfigurator, ApplicationLoader, Application}

class ApplicationComponents(context: Context)
    extends Routes(context)
    with play.filters.HttpFiltersComponents {

  LoggerConfigurator(context.environment.classLoader).foreach {
    _.configure(context.environment, context.initialConfiguration, Map.empty)
  }

}

class Loader extends ApplicationLoader {
  def load(context: Context): Application = new ApplicationComponents(
    context
  ).application
}
