package application
import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext

import scala.concurrent.ExecutionContext

trait DatabaseExecutionContext extends ExecutionContext

class DatabaseExecutionContextImpl(system: ActorSystem)
    extends CustomExecutionContext(system, "database-context")
    with DatabaseExecutionContext
