package testsetup

import devcontainers.PostgresAppContainer
import org.testcontainers.containers.GenericContainer

trait PostgresTest extends TestContainersApplicationFactory with PostgresAppContainer {
  override def containers: Seq[GenericContainer[_]] = Seq(postgresContainer)
  override def containerConfiguration: Map[String, Any] = postgresContainerConfiguration
}

