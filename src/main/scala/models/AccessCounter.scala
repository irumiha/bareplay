package models

import anorm._
import application.DatabaseExecutionContext
import play.api.db.Database

import java.time.LocalDateTime
import scala.concurrent.Future

case class AccessCounterRow(
    id: Long = 0,
    counter: Long = 0,
    lastUpdate: LocalDateTime = LocalDateTime.now()
)
object AccessCounterRow {
  val rowParser: RowParser[AccessCounterRow] =
    Macro.namedParser[AccessCounterRow](Macro.ColumnNaming.SnakeCase)
}

class AccessCounterRepository(
    val database: Database,
    dbCtx: DatabaseExecutionContext
) extends BaseRepository(dbCtx) {

  def fetchById(counterId: Long): Future[Option[AccessCounterRow]] = Future {
    database.withConnection { implicit c =>
      SQL"""select * from access_counter where id = $counterId"""
        .as(AccessCounterRow.rowParser.singleOpt)
    }
  }

  def persistNew(accessCounterRow: AccessCounterRow): Future[AccessCounterRow] = Future {
    database.withConnection { implicit c =>
      SQL"""
      insert into access_counter(counter) values (${accessCounterRow.counter})
      RETURNING *
      """.as(AccessCounterRow.rowParser.single)
    }
  }

  def persistExisting(accessCounterRow: AccessCounterRow): Future[AccessCounterRow] = Future {
    database.withConnection { implicit c =>
      SQL"""
        update access_counter
        set counter = ${accessCounterRow.counter}, last_update=${accessCounterRow.lastUpdate}
        where id = ${accessCounterRow.id}
        RETURNING *
        """.as(AccessCounterRow.rowParser.single)
    }
  }

  def increment(counterId: Long): Future[Option[AccessCounterRow]] = Future {
    database.withTransaction { implicit c =>
      SQL"""
      UPDATE access_counter SET counter=counter+1 where id=$counterId
      RETURNING *
  """.as(AccessCounterRow.rowParser.singleOpt)
    }
  }
}