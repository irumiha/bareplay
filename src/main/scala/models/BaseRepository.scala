package models

import application.DatabaseExecutionContext

abstract class BaseRepository(dbCtx: DatabaseExecutionContext) {
  implicit val databaseExecutionContext: DatabaseExecutionContext = dbCtx
}
