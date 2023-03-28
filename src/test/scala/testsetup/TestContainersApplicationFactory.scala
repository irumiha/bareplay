package testsetup

import akka.Done
import akka.actor.CoordinatedShutdown
import devcontainers.DevContainersComponent
import play.api.{Application, ApplicationLoader, Configuration}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait TestContainersApplicationFactory extends TestedApplicationFactory with DevContainersComponent {

  override def applicationContext: ApplicationLoader.Context = {
    containers.foreach(c => c.start())

    val ac                          = super.applicationContext
    val configurationFromContainers = Configuration.from(containerConfiguration)

    ac.copy(initialConfiguration =
      configurationFromContainers.withFallback(ac.initialConfiguration)
    )
  }

  override def build(): Application = {
    val builtApplication = super.build()

    builtApplication.coordinatedShutdown
      .addTask(CoordinatedShutdown.PhaseActorSystemTerminate, "stop-testcontainers") { () =>
        Future {
          containers.foreach { c =>
            try {
              c.stop()
            } catch {
              case x: Exception =>
                x.printStackTrace(System.err)
            }
          }
          Done
        }
      }

    builtApplication
  }
}
