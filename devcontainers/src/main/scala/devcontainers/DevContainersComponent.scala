package devcontainers

import org.testcontainers.containers.GenericContainer

trait DevContainersComponent {

  def containers: Seq[GenericContainer[_]]
  def containerConfiguration: Map[String, Any]

}
