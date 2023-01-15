package testsetup
import org.testcontainers.containers.GenericContainer

trait AllAppContainersTest extends PostgresContainerTest with KeycloakContainerTest {

  override def containers: Seq[GenericContainer[_]] = Seq(
    postgresContainer,
    keycloakContainer
  )

  override def containerConfiguration: Map[String, Any] =
    postgresContainerConfiguration.concat(keycloakContainerConfiguration)
}
