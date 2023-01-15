package testsetup
import akka.Done
import akka.actor.CoordinatedShutdown
import org.testcontainers.containers.GenericContainer
import play.api.{Application, ApplicationLoader, Configuration}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait TestContainersApplicationFactory extends TestedApplicationFactory {

  def containers: Seq[GenericContainer[_]]
  def containerConfiguration: Map[String, Any]

  override def applicationContext: ApplicationLoader.Context = {
    containers.foreach(c => c.start())

    val ac = super.applicationContext
    val configurationFromContainers = Configuration.from(containerConfiguration)

    ac.copy(initialConfiguration = configurationFromContainers.withFallback(ac.initialConfiguration))
  }

  override def build(): Application = {
    val builtApplication = super.build()

    builtApplication.coordinatedShutdown
      .addTask(CoordinatedShutdown.PhaseActorSystemTerminate, "stop-testcontainers") { () => Future {
        containers.foreach{ c =>
          try {
            c.stop()
          } catch {
            case x: Exception =>
              x.printStackTrace(System.err)
          }
        }
        Done
      }}

    builtApplication
  }
}
