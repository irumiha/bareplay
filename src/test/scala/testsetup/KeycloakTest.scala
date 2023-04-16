package testsetup

import devcontainers.KeycloakAppContainer
import org.testcontainers.containers.GenericContainer

trait KeycloakTest extends TestContainersApplicationFactory with KeycloakAppContainer:
  override def realmName: String                        = "test-realm"
  override def containers: Seq[GenericContainer[?]]     = Seq(keycloakContainer)
  override def containerConfiguration: Map[String, Any] = keycloakContainerConfiguration
