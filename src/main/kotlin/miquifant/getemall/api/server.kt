/**
 * Entry point for getemall's REST API, and start server function.
 *
 * Created by miquifant on 2020-10-24
 */
package miquifant.getemall.api

import miquifant.getemall.api.authentication.impl.UserListDao
import miquifant.getemall.api.controller.Admin
import miquifant.getemall.api.controller.IndexController
import miquifant.getemall.api.controller.LoginController
import miquifant.getemall.api.controller.PagesController
import miquifant.getemall.api.controller.ServiceController
import miquifant.getemall.api.controller.Web
import miquifant.getemall.command.Opts
import miquifant.getemall.command.loadFullConfig
import miquifant.getemall.utils.GrantedFor

import com.typesafe.config.Config
import io.javalin.Javalin
import io.javalin.http.staticfiles.Location


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
    conf.addStaticFiles("assets", Location.CLASSPATH)
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
    get  ("/",                   ServiceController.liveness,   GrantedFor.anyone)
    get  (Admin.Uri.LIVENESS,    ServiceController.liveness,   GrantedFor.anyone)
    get  (Admin.Uri.READINESS,   ServiceController.readiness,  GrantedFor.anyone)
    get  (Admin.Uri.METADATA,    ServiceController.metadata,   GrantedFor.anyone)
    post (Admin.Uri.KILL,        ServiceController.kill(this), GrantedFor.admins)

    /* =========================================================================================
     *  Web
     * ========================================================================================= */
    // Just some error handler examples
    error (500, "html", Web.internalError)
    error (404, "html", Web.notFound)
    error (401, "html", Web.unauthorized)
    // Filters
    before(Web.handleLocaleChange)

    get  (Web.Uri.INDEX,  IndexController.serveIndexPage,           GrantedFor.anyone)
    get  (Web.Uri.LOGIN,  LoginController.serveLoginPage,           GrantedFor.anyone)
    post (Web.Uri.LOGIN,  LoginController.handleLoginPost(userDao), GrantedFor.anyone)
    post (Web.Uri.LOGOUT, LoginController.handleLogoutPost,         GrantedFor.anyone)

    get  (Web.Uri.VIEW1, PagesController.servePage1, GrantedFor.admins)
  }
  .start(port)
}
