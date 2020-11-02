/**
 * Utilities and common functions for working with Javalin.
 *
 * Created by miquifant on 2020-10-25
 */
package miquifant.getemall.utils

import miquifant.getemall.utils.AppRole.*

import io.javalin.core.security.Role
import io.javalin.core.util.Header
import io.javalin.http.Context
import io.javalin.http.Handler


typealias Handler = (Context) -> Unit
typealias ExceptionHandler = (Exception, Context) -> Unit
typealias AccessManager = (Handler, Context, Set<Role>) -> Unit
typealias RequestLogger = (Context, Float) -> Unit

enum class AppRole: Role {
  ANONYMOUS, REGULAR_USER, ADMIN
}

internal object GrantedFor {
  val admins        = setOf(ADMIN)
  val loggedInUsers = admins + setOf(REGULAR_USER)
  val anyone        = loggedInUsers + setOf(ANONYMOUS)
}

/**
 * Use this method when you have a velocity template and messages from a Resource Bundle.
 * Resource Bundles use encoding iso-8859-1 by default, so the rendered documents will use that encoding.
 * This extension function calls the native render after setting response's characterEncoding to 'iso-8859-1'.
 *
 * Renders a file with specified values and sets it as the context result.
 * Also sets content-type to text/html with encoding iso-8859-1
 * Determines the correct rendering-function based on the file extension.
 */
fun Context.renderLatin1(filePath: String, model: Map<String, Any?>): Context {
  this.res.characterEncoding = "iso-8859-1"
  return this.render(filePath, model)
}
