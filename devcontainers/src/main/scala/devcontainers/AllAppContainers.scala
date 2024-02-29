package devcontainers

import org.testcontainers.containers.GenericContainer

trait AllAppContainers extends PostgresAppContainer with KeycloakAppContainer {

  override def containers: Seq[GenericContainer[?]] = Seq(
    postgresContainer,
    keycloakContainer
  )

  override def containerConfiguration: Map[String, Any] =
    postgresContainerConfiguration.concat(keycloakContainerConfiguration)
}
