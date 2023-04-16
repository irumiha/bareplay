package application

import devcontainers.AllAppContainers
import org.testcontainers.containers.GenericContainer
import play.api.ApplicationLoader.Context
import play.api.{
  Application,
  ApplicationLoader,
  BuiltInComponentsFromContext,
  Configuration
}

class DevLoader extends ApplicationLoader with AllAppContainers:

  override def realmName: String = "dev-realm"

  override def containers: Seq[GenericContainer[?]] =
    Seq(postgresContainer.withReuse(true), keycloakContainer.withReuse(true))

  override def load(context: Context): Application =
    containers.foreach(c => c.start())

    val configurationFromContainers = Configuration.from(containerConfiguration)

    val devContext = context.copy(initialConfiguration =
      configurationFromContainers.withFallback(context.initialConfiguration)
    )

    (new BuiltInComponentsFromContext(devContext) with ApplicationComponents).application
