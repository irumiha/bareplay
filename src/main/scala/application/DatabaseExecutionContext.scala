package application
import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext

import scala.concurrent.ExecutionContext

// Make sure to bind the new context class to this trait using one of the custom
// binding techniques listed on the "Scala Dependency Injection" documentation page
trait DatabaseExecutionContext extends ExecutionContext

class DatabaseExecutionContextImpl(system: ActorSystem)
    extends CustomExecutionContext(system, "database-context")
    with DatabaseExecutionContext
