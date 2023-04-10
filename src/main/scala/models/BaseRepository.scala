package models

import application.DatabaseExecutionContext
import play.api.Logging

abstract class BaseRepository(dbCtx: DatabaseExecutionContext) extends Logging:
  implicit val databaseExecutionContext: DatabaseExecutionContext = dbCtx
