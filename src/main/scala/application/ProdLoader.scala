package application

import play.api.ApplicationLoader.Context
import play.api.{
  Application,
  ApplicationLoader,
  BuiltInComponentsFromContext,
  LoggerConfigurator
}

class ProdLoader extends ApplicationLoader:
  def load(context: Context): Application =
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment, context.initialConfiguration, Map.empty)
    }
    (new BuiltInComponentsFromContext(context) with ApplicationComponents).application
