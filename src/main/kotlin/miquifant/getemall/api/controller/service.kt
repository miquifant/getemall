/**
 * URIs and general purpose Handlers for service.
 *
 * Created by miquifant on 2020-10-25
 */
package miquifant.getemall.api.controller

import miquifant.getemall.api.authentication.User
import miquifant.getemall.api.authentication.UserDao
import miquifant.getemall.utils.*

import io.javalin.Javalin

import java.util.*

import kotlin.concurrent.thread


// TODO routing: Should I change /api/* to /api/{version}/* ?
object Admin {
  object Uri {
    const val LIVENESS    = "/api/admin/liveness"
    const val READINESS   = "/api/admin/readiness"
    const val METADATA    = "/api/admin/metadata"
    const val KILL        = "/api/admin/stop"
  }
}

object ServiceController {

  val accessManager: AccessManager = { handler, ctx, permittedRoles ->
    val effectivePermitedRoles = if (permittedRoles.isEmpty()) GrantedFor.loggedInUsers else permittedRoles
    // sessionizeUser filter should have authenticated creds and stored user in session
    val user = ctx.sessionAttribute<User?>("curUser")
    val role = user?.role ?: AppRole.ANONYMOUS
    if (effectivePermitedRoles.contains(role)) handler.handle(ctx)
    else {
      println("*** Access denied to '${ctx.matchedPath()}' for user '${user?.name ?: "anonymous"}' with role '$role'")
      // Capture it with a Content-Type:"html" specific 401 error filter, to redirect to login or unauthorized page
      ctx.status(401).result("Unauthorized\n")
    }
  }

  val requestLogger: RequestLogger = { ctx, _ ->
    val ip = ctx.ip()
    val date = formatISO8601Date(Date())
    val action = ctx.req.method
    val uri = ctx.req.requestURI
    val protocol = ctx.protocol()
    val status = ctx.res.status
    val user = ctx.sessionAttribute<User?>("curUser")?.name ?: "anonymous"

    println("""$ip [$date] "$action $uri $protocol" $status - $user""")
  }

  val exceptionHandler: ExceptionHandler = { e, ctx ->
    e.printStackTrace()
    ctx.status(500).json(e)
  }

  /**
   * Stores in session running user based on request
   */
  val sessionizeUser: (UserDao) -> Handler = { userDao ->
    { ctx ->
      val user: User? = if (ctx.basicAuthCredentialsExist())
        userDao.authenticate(ctx.basicAuthCredentials().username, ctx.basicAuthCredentials().password)
      else null
      if (user != null && ctx.sessionAttribute<User?>("curUser") != user) ctx.sessionAttribute("curUser", user)
    }
  }

  // ----------------------------------------------------------------------------------------------
  // Endpoint Handlers
  // ----------------------------------------------------------------------------------------------
  val liveness: Handler = { ctx ->
    ctx.result("getemall is alive\n")
  }
  val readiness: Handler = { ctx ->
    // TODO Implement here all needed checks
    ctx.result("getemall is ready\n")
  }
  val metadata: Handler = { ctx ->
    ctx.json(retrieveAppMetadata())
  }
  val kill: (Javalin) -> Handler = { app ->
    { ctx ->
      ctx.result("bye!\n")
      thread { app.stop() }
    }
  }
}
