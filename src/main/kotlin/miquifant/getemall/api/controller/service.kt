/**
 * URIs and general purpose Handlers for service.
 *
 * Created by miquifant on 2020-10-25
 */
package miquifant.getemall.api.controller

import miquifant.getemall.api.authentication.User
import miquifant.getemall.api.authentication.UserDao
import miquifant.getemall.api.controller.ComponentStatus.DOWN
import miquifant.getemall.api.controller.ComponentStatus.OK
import miquifant.getemall.log.Loggable.Logger
import miquifant.getemall.log.LoggerFactory
import miquifant.getemall.model.ExceptionalResponse
import miquifant.getemall.utils.*

import io.javalin.Javalin

import kotlin.concurrent.thread


object Admin {
  object Uri {
    const val LOGIN_STATE = "/api/admin/loginState"
    const val LIVENESS    = "/api/admin/liveness"
    const val READINESS   = "/api/admin/readiness"
    const val METADATA    = "/api/admin/metadata"
    const val KILL        = "/api/admin/stop"
  }
}

data class LoginState(val authError: Boolean,
                      val authFailed: Boolean,
                      val authSucceeded: Boolean,
                      val loggedOut: Boolean,
                      val redirect: String?)
{
  companion object {
    const val AUTH_ERROR     = "loginState.authError"     // Log in service failed
    const val AUTH_FAILED    = "loginState.authFailed"    // Wrong username or password
    const val AUTH_SUCCEEDED = "loginState.authSucceeded"
    const val LOGGED_OUT     = "loginState.loggedOut"
    const val REDIRECT       = "loginState.redirect"
  }
}

enum class ComponentStatus { OK, DOWN }

data class ComponentCheck(val status: ComponentStatus,
                          val message: String? = null)

data class ServiceCheck(val status: ComponentStatus,
                        val components: Map<String, ComponentCheck>,
                        val message: String? = null)

object ServiceController {

  private val logger = LoggerFactory.logger(ServiceController::class.java.canonicalName)

  fun checkLogin(dao: UserDao): ComponentCheck = try {

    dao.getUserByUsername("admin")

    ComponentCheck(OK)
  } catch (e: Exception) {
    val msg = "Login component not ready"
    logger.errorWithThrowable(e) { msg }
    ComponentCheck(DOWN, msg)
  }

  fun checkDB(db: ConnectionManager): ComponentCheck = try {

    db()

    ComponentCheck(OK)
  } catch (e: Exception) {
    val msg = "Database component not ready"
    logger.errorWithThrowable(e) { msg }
    ComponentCheck(DOWN, msg)
  }

  val accessManager: (Logger) -> AccessManager = { accessLogger ->
    { handler, ctx, permittedRoles ->
      val effectivePermitedRoles = if (permittedRoles.isEmpty()) GrantedFor.loggedInUsers else permittedRoles
      // sessionizeUser filter should have authenticated creds and stored user in session
      val user = ctx.sessionAttribute<User?>("curUser")
      val role = user?.role ?: AppRole.ANONYMOUS
      if (effectivePermitedRoles.contains(role)) handler.handle(ctx)
      else {
        accessLogger.info {
          "Access denied to '${ctx.matchedPath()}' for user '${user?.name ?: "anonymous"}' with role '$role'"
        }
        // Capture it with a Content-Type:"html" specific 401 error filter, to redirect to login or unauthorized page
        ctx.exception(ExceptionalResponse.unauthorized)
      }
    }
  }

  val requestLogger: (Logger) -> RequestLogger = { navigationLogger ->
    { ctx, _ ->
      val ip = ctx.ip()
      val action = ctx.req.method
      val uri = ctx.req.requestURI
      val protocol = ctx.protocol()
      val status = ctx.res.status
      val user = ctx.sessionAttribute<User?>("curUser")?.name ?: "anonymous"

      navigationLogger.info { """$ip "$action $uri $protocol" $status - $user""" }
    }
  }

  val javalinVueState: JavalinState = { ctx ->
    mapOf (
        "currentUser" to ctx.sessionAttribute<User?>("curUser")?.name,
        "currentRole" to ctx.sessionAttribute<User?>("curUser")?.role
    )
  }

  val exceptionHandler: ExceptionHandler = { e, ctx ->
    logger.errorWithThrowable(e) { "An error occurred: ${e.message}" }
    // TODO As the application becomes more "production ready" this response should disappear
    ctx.status(500).json(e)
  }

  /**
   * Stores in session running user based on request
   */
  val sessionizeUser: (UserDao, Logger) -> Handler = { userDao, accessLogger ->
    { ctx ->
      val user: User? = if (ctx.basicAuthCredentialsExist())
        try {
          userDao.authenticate(ctx.basicAuthCredentials().username, ctx.basicAuthCredentials().password, accessLogger)
        } catch (e: Exception) { null }
      else null
      if (user != null && ctx.sessionAttribute<User?>("curUser") != user) ctx.sessionAttribute("curUser", user)
    }
  }

  // ----------------------------------------------------------------------------------------------
  // Endpoint Handlers
  // ----------------------------------------------------------------------------------------------
  val loginState: Handler = { ctx ->

    val authError = ctx.sessionAttribute(LoginState.AUTH_ERROR) ?: false
    ctx.sessionAttribute(LoginState.AUTH_ERROR, null)

    val authFailed = ctx.sessionAttribute(LoginState.AUTH_FAILED) ?: false
    ctx.sessionAttribute(LoginState.AUTH_FAILED, null)

    val authSucceeded = ctx.sessionAttribute(LoginState.AUTH_SUCCEEDED) ?: false
    ctx.sessionAttribute(LoginState.AUTH_SUCCEEDED, null)

    val loggedOut = ctx.sessionAttribute(LoginState.LOGGED_OUT) ?: false
    ctx.sessionAttribute(LoginState.LOGGED_OUT, null)

    val loginRedirect = ctx.sessionAttribute<String?>(LoginState.REDIRECT)
    ctx.sessionAttribute(LoginState.REDIRECT, null)

    ctx.json(LoginState(authError, authFailed, authSucceeded, loggedOut, loginRedirect))
  }

  val liveness: Handler = { ctx ->
    ctx.json(ComponentCheck(OK, "getemall is alive"))
  }

  val readiness: (UserDao, ConnectionManager) -> Handler = { userDao, db ->
    { ctx ->

      // Check Login state
      val loginStatus: ComponentCheck = checkLogin(userDao)

      // Check DB state
      val dbStatus: ComponentCheck = checkDB(db)

      // Gather all component statuses
      val components = mapOf (
          "login_check" to loginStatus,
          "db_check" to dbStatus
      )
      val serviceStatus = if (components.values.any { check -> check.status == DOWN }) DOWN else OK
      ctx.json(ServiceCheck(
          status = serviceStatus,
          components = components,
          message = "Service ${if (serviceStatus == OK) "is" else "NOT" } ready"
      ))
    }
  }

  val metadata: Handler = { ctx ->
    ctx.json(retrieveAppMetadata())
  }

  val kill: (Javalin) -> Handler = { app ->
    { ctx ->
      ctx.result("bye!\n")
      logger.info { "Service stopped by '${ctx.sessionAttribute<User?>("curUser")?.name ?: "anonymous"}'" }
      thread { app.stop() }
    }
  }
}
