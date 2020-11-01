/**
 * Entry point for getemall's REST API, and start server function.
 *
 * Created by miquifant on 2020-10-24
 */
package miquifant.getemall.api

import miquifant.getemall.api.authentication.impl.UserListDao
import miquifant.getemall.api.controller.Admin
import miquifant.getemall.api.controller.ServiceController
import miquifant.getemall.command.Opts
import miquifant.getemall.command.loadFullConfig
import miquifant.getemall.utils.GrantedFor

import com.typesafe.config.Config
import io.javalin.Javalin


const val DEFAULT_PORT = 8080

fun startServer(opts: Opts): Javalin {

  val config: Config = loadFullConfig (
      if (opts["--conf"] != null && (opts["--conf"] as String).isNotEmpty()) opts["--conf"] as String
      else "empty.conf"
  )
  val port = if (config.hasPath("api_port")) config.getInt("api_port") else DEFAULT_PORT

  val userDao = UserListDao()

  return Javalin.create { conf ->

    // Configure service
    conf.defaultContentType = "application/json; charset=utf-8"
    conf.accessManager(ServiceController.accessManager)
    conf.requestLogger(ServiceController.requestLogger)

  }.apply {

    /* =========================================================================================
     *  Define EXCEPTION and ERROR handlers, and General FILTERS
     * ========================================================================================= */
    exception(Exception::class.java, ServiceController.exceptionHandler)
    before (ServiceController.sessionizeUser(userDao))

    /* =========================================================================================
     *  Endpoints
     * ========================================================================================= */
    get  ("/",                 ServiceController.liveness,   GrantedFor.anyone)
    get  (Admin.Uri.LIVENESS,  ServiceController.liveness,   GrantedFor.anyone)
    get  (Admin.Uri.READINESS, ServiceController.readiness,  GrantedFor.anyone)
    get  (Admin.Uri.METADATA,  ServiceController.metadata,   GrantedFor.anyone)

    post (Admin.Uri.KILL,      ServiceController.kill(this), GrantedFor.admins)
  }
  .start(port)
}
