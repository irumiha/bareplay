package testsetup

import org.testcontainers.containers.{GenericContainer, PostgreSQLContainer}

trait PostgresContainerTest extends TestContainersApplicationFactory {
  // Testcontainers have an unusual API, even when used from Java.
  class PgContainer(imageName: String) extends PostgreSQLContainer[PgContainer](imageName)

  private val tmpfsMount = new java.util.HashMap[String, String]()
  tmpfsMount.put("/var/lib/postgresql/data", "rw")

  protected val postgresContainer: PgContainer =
    new PgContainer("postgres:15")
      .withTmpFs(tmpfsMount)

  protected def postgresContainerConfiguration: Map[String, Any] = Map(
    "db.default.driver"   -> "org.postgresql.Driver",
    "db.default.url"      -> postgresContainer.getJdbcUrl,
    "db.default.username" -> postgresContainer.getUsername,
    "db.default.password" -> postgresContainer.getPassword
  )

  override def containers: Seq[GenericContainer[_]]     = Seq(postgresContainer)
  override def containerConfiguration: Map[String, Any] = postgresContainerConfiguration
}
