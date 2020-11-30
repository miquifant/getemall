/**
 * Entry point for getemall's REST API, and start server function.
 *
 * Created by miquifant on 2020-10-24
 */
package miquifant.getemall.api

import miquifant.getemall.api.authentication.impl.UserListDao
import miquifant.getemall.api.controller.Admin
import miquifant.getemall.api.controller.LoginController
import miquifant.getemall.api.controller.ServiceController
import miquifant.getemall.api.controller.Web
import miquifant.getemall.command.Opts
import miquifant.getemall.command.loadFullConfig
import miquifant.getemall.utils.GrantedFor
import miquifant.getemall.utils.retrieveAppMetadata

import com.typesafe.config.Config
import io.javalin.Javalin
import io.javalin.http.staticfiles.Location
import io.javalin.plugin.rendering.vue.JavalinVue
import io.javalin.plugin.rendering.vue.VueComponent


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
    conf.enableWebjars()

    JavalinVue.stateFunction = ServiceController.javalinVueState

    val isDev = retrieveAppMetadata().version == "test-version"
    JavalinVue.isDevFunction = { isDev }

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
    get  (Admin.Uri.LOGIN_STATE, ServiceController.loginState, GrantedFor.anyone)
    get  (Admin.Uri.LIVENESS,    ServiceController.liveness,   GrantedFor.anyone)
    get  (Admin.Uri.READINESS,   ServiceController.readiness,  GrantedFor.anyone)
    get  (Admin.Uri.METADATA,    ServiceController.metadata,   GrantedFor.anyone)
    post (Admin.Uri.KILL,        ServiceController.kill(this), GrantedFor.admins)

    /* =========================================================================================
     *  Web
     * ========================================================================================= */
    // Just some error handler examples
    error (404, "html", VueComponent("not-found"))
    error (401, "html", Web.unauthorized)

    get  (Web.Uri.INDEX,  VueComponent("index"),                    GrantedFor.anyone)
    get  (Web.Uri.LOGIN,  VueComponent("login"),                    GrantedFor.anyone)
    post (Web.Uri.LOGIN,  LoginController.handleLoginPost(userDao), GrantedFor.anyone)
    post (Web.Uri.LOGOUT, LoginController.handleLogoutPost,         GrantedFor.anyone)

    get  (Web.Uri.VIEW1, VueComponent("view-1"), GrantedFor.anyone)
    get  (Web.Uri.VIEW2, VueComponent("view-2"), GrantedFor.loggedInUsers)
    get  (Web.Uri.VIEW3, VueComponent("view-3"), GrantedFor.admins)
  }
  .start(port)
}
