package testsetup

import org.testcontainers.containers.{GenericContainer, PostgreSQLContainer}

trait PostgresContainerTest extends TestContainersApplicationFactory {
  // Testcontainers have an unusual API, even when used from Java.
  class PgContainer(imageName: String) extends PostgreSQLContainer[PgContainer](imageName)
  val postgresContainer = new PgContainer("postgres:15")

  override def containers: Seq[GenericContainer[_]] = Seq(postgresContainer)
  override def containerConfiguration: Map[String, Any] = Map(
    "db.default.driver"   -> "org.postgresql.Driver",
    "db.default.url"      -> postgresContainer.getJdbcUrl,
    "db.default.username" -> postgresContainer.getUsername,
    "db.default.password" -> postgresContainer.getPassword
  )
}
