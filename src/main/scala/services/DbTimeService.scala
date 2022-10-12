package services

import java.sql.Connection
import java.time.LocalDateTime

object DbTimeService {

  def getCurrentTimeFromDb(implicit conn: Connection): LocalDateTime = {
    val resp = conn
      .prepareStatement(
        "select formatdatetime(current_timestamp, 'yyyy-MM-dd''T''HH:mm:ss')"
      )
      .executeQuery()
    val dt = if (resp.next()) {
      val dbDateTime = resp.getString(1)
      LocalDateTime.parse(dbDateTime)
    } else {
      LocalDateTime.now()
    }

    resp.close()
    dt
  }

  def incAndGetCounter(implicit conn: Connection): Int = {
    val updateStmt =
      conn.prepareStatement("update access_counter set counter = counter + 1")
    updateStmt.execute()
    updateStmt.close()

    val counterStmt = conn
      .prepareStatement(
        "select counter from access_counter where id = 1"
      )
    val resp = counterStmt.executeQuery()
    val counter = if (resp.next()) {
      resp.getInt(1)
    } else {
      0
    }

    counterStmt.close()
    counter
  }

}
