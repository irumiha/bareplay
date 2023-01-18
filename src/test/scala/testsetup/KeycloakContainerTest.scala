package testsetup

import dasniko.testcontainers.keycloak.KeycloakContainer
import org.testcontainers.containers.GenericContainer

trait KeycloakContainerTest extends TestContainersApplicationFactory {
  def realmName: String

  protected val keycloakContainer: KeycloakContainer =
    new KeycloakContainer()

  protected def keycloakContainerConfiguration: Map[String, Any] = Map(
    "security.oauth2_oidc.auth_url" -> s"${keycloakContainer.getAuthServerUrl}/realms/$realmName/protocol/openid-connect/auth",
    "security.oauth2_oidc.token_url" -> s"${keycloakContainer.getAuthServerUrl}/realms/$realmName/protocol/openid-connect/token",
    "security.oauth2_oidc.token_issuer" -> s"${keycloakContainer.getAuthServerUrl}/realms/$realmName",
    "security.oauth2_oidc.jwt_signing_public_key" -> s""
  )

  override def containers: Seq[GenericContainer[_]]     = Seq(keycloakContainer)
  override def containerConfiguration: Map[String, Any] = keycloakContainerConfiguration
}
