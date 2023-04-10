package application.security

import play.api.cache.AsyncCacheApi

case class AuthenticationCache(cache: AsyncCacheApi) extends AnyVal
