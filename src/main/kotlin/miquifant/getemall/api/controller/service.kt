/**
 * URIs and general purpose Handlers for service.
 *
 * Created by miquifant on 2020-10-25
 */
package miquifant.getemall.api.controller

import miquifant.getemall.api.authentication.User
import miquifant.getemall.api.authentication.UserDao
import miquifant.getemall.log.Loggable.Logger
import miquifant.getemall.log.LoggerFactory
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

data class LoginState(val authFailed: Boolean,
                      val authSucceeded: Boolean,
                      val loggedOut: Boolean,
                      val redirect: String?)
{
  companion object {
    const val AUTH_FAILED    = "loginState.authFailed"
    const val AUTH_SUCCEEDED = "loginState.authSucceeded"
    const val LOGGED_OUT     = "loginState.loggedOut"
    const val REDIRECT       = "loginState.redirect"
  }
}

object ServiceController {

  private val logger = LoggerFactory.logger(ServiceController::class.java.canonicalName)

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
        ctx.status(401).result("Unauthorized\n")
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
    ctx.status(500).json(e)
  }

  /**
   * Stores in session running user based on request
   */
  val sessionizeUser: (UserDao, Logger) -> Handler = { userDao, accessLogger ->
    { ctx ->
      val user: User? = if (ctx.basicAuthCredentialsExist())
        userDao.authenticate(ctx.basicAuthCredentials().username, ctx.basicAuthCredentials().password, accessLogger)
      else null
      if (user != null && ctx.sessionAttribute<User?>("curUser") != user) ctx.sessionAttribute("curUser", user)
    }
  }

  // ----------------------------------------------------------------------------------------------
  // Endpoint Handlers
  // ----------------------------------------------------------------------------------------------
  val loginState: Handler = { ctx ->

    val authFailed = ctx.sessionAttribute(LoginState.AUTH_FAILED) ?: false
    ctx.sessionAttribute(LoginState.AUTH_FAILED, null)

    val authSucceeded = ctx.sessionAttribute(LoginState.AUTH_SUCCEEDED) ?: false
    ctx.sessionAttribute(LoginState.AUTH_SUCCEEDED, null)

    val loggedOut = ctx.sessionAttribute(LoginState.LOGGED_OUT) ?: false
    ctx.sessionAttribute(LoginState.LOGGED_OUT, null)

    val loginRedirect = ctx.sessionAttribute<String?>(LoginState.REDIRECT)
    ctx.sessionAttribute(LoginState.REDIRECT, null)

    ctx.json(LoginState(authFailed, authSucceeded, loggedOut, loginRedirect))
  }

  val liveness: Handler = { ctx ->
    ctx.result("getemall is alive\n")
  }
  val readiness: Handler = { ctx ->
    // Add here all necessary checks
    ctx.result("getemall is ready\n")
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