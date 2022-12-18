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

  def persistRow(accessCounterRow: AccessCounterRow): Future[Long] = Future {
    database.withConnection { implicit c =>
      SQL"""insert into access_counter(counter) values (${accessCounterRow.counter})"""
        .executeInsert(SqlParser.long(1).single)
    }
  }

  def increment(counterId: Long): Future[Option[Long]] = Future {
    database.withConnection { implicit c =>
        SQL"""update access_counter
          set counter=counter+1
          where id = $counterId
          returning counter"""
        .as(SqlParser.long(1).singleOpt)
        .orElse{
          SQL"""
              insert into access_counter(id, counter)
              values ($counterId, 0)
              returning counter"""
          .as(SqlParser.long(1).singleOpt)
        }
    }
  }
}
