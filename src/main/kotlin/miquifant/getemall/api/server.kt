@file:JvmName("GetemallServer")
/**
 * Entry point for getemall's REST API, and start server function.
 *
 * Created by miquifant on 2020-10-24
 */
package miquifant.getemall.api

import miquifant.getemall.api.authentication.impl.UserDatabaseDao
import miquifant.getemall.api.controller.Admin
import miquifant.getemall.api.controller.LoginController
import miquifant.getemall.api.controller.Profiles
import miquifant.getemall.api.controller.ServiceController
import miquifant.getemall.api.controller.ProfilesController
import miquifant.getemall.api.controller.Web
import miquifant.getemall.command.Opts
import miquifant.getemall.command.loadFullConfig
import miquifant.getemall.log.LoggerFactory
import miquifant.getemall.utils.ConnectionManager
import miquifant.getemall.utils.GrantedFor
import miquifant.getemall.utils.connectionManager
import miquifant.getemall.utils.retrieveAppMetadata

import com.typesafe.config.Config
import io.javalin.Javalin
import io.javalin.http.staticfiles.Location
import io.javalin.plugin.rendering.vue.JavalinVue
import io.javalin.plugin.rendering.vue.VueComponent


const val DEFAULT_PORT = 8080

private val logger           = LoggerFactory.logger("GetemallServer")
private val accessLogger     = LoggerFactory.logger("AccessManager")
private val navigationLogger = LoggerFactory.logger("NavigationLog")

fun startServer(opts: Opts): Javalin {

  val config: Config = loadFullConfig (
      if (opts["--conf"] != null && (opts["--conf"] as String).isNotEmpty()) opts["--conf"] as String
      else "empty.conf"
  )
  val port = if (config.hasPath("api_port")) config.getInt("api_port") else DEFAULT_PORT

  val db: ConnectionManager = connectionManager(config)

  val userDao = UserDatabaseDao(db)

  return Javalin.create { conf ->

    val isDev = retrieveAppMetadata().version == "test-version"
    logger.info { "Application starting in ${if (isDev) "development" else "production"} mode" }
    JavalinVue.isDevFunction = { isDev }

    logger.debug { "Configuring javalin..." }

    // Configure service
    conf.defaultContentType = "application/json; charset=utf-8"
    conf.addStaticFiles("assets", Location.CLASSPATH)
    conf.accessManager(ServiceController.accessManager(accessLogger))
    conf.requestLogger(ServiceController.requestLogger(navigationLogger))
    conf.enableWebjars()

    logger.debug { "Setting javalinVueState function..." }
    JavalinVue.stateFunction = ServiceController.javalinVueState

  }.apply {

    /* =========================================================================================
     *  Define EXCEPTION and ERROR handlers, and General FILTERS
     * ========================================================================================= */
    logger.debug { "Defining error handlers and filters..." }
    exception(Exception::class.java, ServiceController.exceptionHandler)
    before (ServiceController.sessionizeUser(userDao, accessLogger))

    /* =========================================================================================
     *  Endpoints
     * ========================================================================================= */
    logger.debug { "Defining API endpoints..." }
    get  ("/",                   ServiceController.liveness,   GrantedFor.anyone)
    get  (Admin.Uri.LOGIN_STATE, ServiceController.loginState, GrantedFor.anyone)
    get  (Admin.Uri.LIVENESS,    ServiceController.liveness,   GrantedFor.anyone)
    get  (Admin.Uri.READINESS,   ServiceController.readiness(userDao, db), GrantedFor.anyone)
    get  (Admin.Uri.METADATA,    ServiceController.metadata,   GrantedFor.anyone)
    post (Admin.Uri.KILL,        ServiceController.kill(this), GrantedFor.admins)

    // Profiles cRUd
    get   (Profiles.Uri.PROFILES,        ProfilesController.getAll(db),              GrantedFor.admins)
    get   (Profiles.Uri.OWN_PROFILE,     ProfilesController.getOwn(db),              GrantedFor.loggedInUsers)
    patch (Profiles.Uri.OWN_PROFILE,     ProfilesController.patchOwn(db),            GrantedFor.loggedInUsers)
    put   (Profiles.Uri.OWN_PUB_PROFILE, ProfilesController.upsertOwnPubProfile(db), GrantedFor.loggedInUsers)
    head  (Profiles.Uri.PROFILE_BY_NAME, ProfilesController.checkUsername(db),       GrantedFor.anyone)
    get   (Profiles.Uri.PROFILE_BY_NAME, ProfilesController.getPublicProfile(db),    GrantedFor.anyone)
    get   (Profiles.Uri.ONE_PROFILE,     ProfilesController.getOne(db),              GrantedFor.admins)

    /* =========================================================================================
     *  Web
     * ========================================================================================= */
    logger.debug { "Defining WEB error handlers..." }
    // Just some error handler examples
    error (404, "html", VueComponent("not-found"))
    error (401, "html", Web.unauthorized)

    logger.debug { "Defining WEB pages..." }
    get  (Web.Uri.INDEX,  VueComponent("index"),                                  GrantedFor.anyone)
    get  (Web.Uri.LOGIN,  VueComponent("login"),                                  GrantedFor.anyone)
    post (Web.Uri.LOGIN,  LoginController.handleLoginPost(userDao, accessLogger), GrantedFor.anyone)
    post (Web.Uri.LOGOUT, LoginController.handleLogoutPost,                       GrantedFor.anyone)

    get  (Web.Uri.SETTINGS_PROFILE,  VueComponent("settings-profile"),  GrantedFor.loggedInUsers)
    get  (Web.Uri.SETTINGS_ACCOUNT,  VueComponent("settings-account"),  GrantedFor.loggedInUsers)

    get  (Web.Uri.PROFILES, VueComponent("profile"), GrantedFor.anyone)

    get  (Web.Uri.VIEW1, VueComponent("view-1"), GrantedFor.anyone)
    get  (Web.Uri.VIEW2, VueComponent("view-2"), GrantedFor.loggedInUsers)
    get  (Web.Uri.VIEW3, VueComponent("view-3"), GrantedFor.admins)
  }
  .start(port)
}
