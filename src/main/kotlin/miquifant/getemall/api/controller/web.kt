/**
 * Web related controllers.
 *
 * Created by miquifant on 2020-10-25
 */
package miquifant.getemall.api.controller

import miquifant.getemall.api.authentication.User
import miquifant.getemall.api.authentication.UserDao
import miquifant.getemall.log.Loggable.Logger
import miquifant.getemall.utils.AppRole.ANONYMOUS
import miquifant.getemall.utils.Handler

import io.javalin.plugin.rendering.vue.VueComponent


object Web {
  object Uri {
    const val LOGIN  = "/login"
    const val LOGOUT = "/logout"
    const val INDEX  = "/index"
    const val SETTINGS_PROFILE = "/settings/profile"
    const val SETTINGS_ACCOUNT = "/settings/account"
    const val SETTINGS_PASSWORD = "/settings/password"
    const val PROFILES = "/profiles/:name"
    const val VIEW1  = "/v1"
    const val VIEW2  = "/v2"
    const val VIEW3  = "/v3"
  }

  val unauthorized: Handler = { ctx ->
    val role = ctx.sessionAttribute<User?>("curUser")?.role ?: ANONYMOUS
    if (role == ANONYMOUS) {
      // The origin of the request (request.pathInfo()) is saved in the session so
      // the user can be redirected back after login
      ctx.sessionAttribute(LoginState.REDIRECT, ctx.path())
      ctx.redirect(Web.Uri.LOGIN)
    }
    else VueComponent("unauthorized").handle(ctx)
  }
}

object LoginController {

  val handleLoginPost: (UserDao, Logger) -> Handler = { userDao, accessLogger ->
    { ctx ->

      val loginRedirect = ctx.formParam("redirect")
      val (succeeded, user) = try {

        Pair(true, userDao.authenticate(ctx.formParam("username"), ctx.formParam("password"), accessLogger))

      } catch (e: Exception) {
        Pair(false, null)
      }
      if (user == null) {
        if (succeeded) ctx.sessionAttribute(LoginState.AUTH_FAILED, true)
        else ctx.sessionAttribute(LoginState.AUTH_ERROR, true)
        ctx.sessionAttribute(LoginState.REDIRECT, loginRedirect)
        ctx.redirect(Web.Uri.LOGIN)
      }
      else {
        ctx.sessionAttribute("curUser", user)
        if (loginRedirect != null) ctx.redirect(loginRedirect)
        else {
          ctx.sessionAttribute(LoginState.AUTH_SUCCEEDED, true)
          ctx.redirect(Web.Uri.LOGIN)
        }
      }
    }
  }

  val handleLogoutPost: Handler = { ctx ->
    ctx.sessionAttribute("curUser", null)
    ctx.sessionAttribute(LoginState.LOGGED_OUT, true)
    ctx.redirect(Web.Uri.LOGIN)
  }
}
