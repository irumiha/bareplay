package testsetup

import devcontainers.AllAppContainers

trait AllContainersTest extends TestContainersApplicationFactory with AllAppContainers:
  override def realmName: String = "test-realm"
