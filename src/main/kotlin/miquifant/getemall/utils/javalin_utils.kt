/**
 * Utilities and common functions for working with Javalin.
 *
 * Created by miquifant on 2020-10-25
 */
package miquifant.getemall.utils

import miquifant.getemall.model.ExceptionalResponse
import miquifant.getemall.model.ExceptionalResponse.Companion.unknownError
import miquifant.getemall.utils.AppRole.*

import io.javalin.core.security.Role
import io.javalin.http.Context
import io.javalin.http.Handler


typealias Handler = (Context) -> Unit
typealias ExceptionHandler = (Exception, Context) -> Unit
typealias AccessManager = (Handler, Context, Set<Role>) -> Unit
typealias RequestLogger = (Context, Float) -> Unit
typealias JavalinState = (Context) -> Any

enum class AppRole: Role {
  ANONYMOUS, REGULAR_USER, ADMIN
}

internal object GrantedFor {
  val admins        = setOf(ADMIN)
  val loggedInUsers = admins + setOf(REGULAR_USER)
  val anyone        = loggedInUsers + setOf(ANONYMOUS)
}

fun Context.exception(code: Int, message: String): Context {
  return this.exception(ExceptionalResponse(code, message))
}

fun Context.exception(res: ExceptionalResponse = unknownError): Context {
  return this.status(res.code).json(res)
}
