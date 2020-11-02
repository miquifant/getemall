/**
 * Web related controllers.
 *
 * Created by miquifant on 2020-10-25
 */
package miquifant.getemall.api.controller

import app.book.bookDao
import miquifant.getemall.api.authentication.User
import miquifant.getemall.api.authentication.UserDao
import miquifant.getemall.utils.AppRole.ANONYMOUS
import miquifant.getemall.utils.Handler
import miquifant.getemall.utils.renderLatin1

import io.javalin.http.Context

import java.text.MessageFormat
import java.util.*


object Web {
  object Uri {
    const val LOGIN  = "/login"
    const val LOGOUT = "/logout"
    const val INDEX  = "/index"
    const val VIEW1  = "/v1"
  }
  object Template {
    const val LOGIN        = "/velocity/login/login.vm"
    const val INDEX        = "/velocity/index/index.vm"
    const val NOT_FOUND    = "/velocity/notFound.vm"
    const val UNAUTHORIZED = "/velocity/unauthorized.vm"
    const val ERROR        = "/velocity/error.vm"
    const val PAGE1        = "/velocity/book/all.vm"
  }

  fun baseModel(ctx: Context): MutableMap<String, Any?> {
    val model = HashMap<String, Any?>()
    model["msg"] = MessageBundle(ctx.sessionAttribute("locale"))
    model["currentUser"] = ctx.sessionAttribute<User>("curUser")?.name
    model["currentRole"] = ctx.sessionAttribute<User>("curUser")?.role
    return model
  }

  val notFound: Handler = { ctx ->
    ctx.renderLatin1(Web.Template.NOT_FOUND, baseModel(ctx))
  }

  val unauthorized: Handler = { ctx ->
    val role = ctx.sessionAttribute<User?>("curUser")?.role ?: ANONYMOUS
    if (role == ANONYMOUS) {
      // The origin of the request (request.pathInfo()) is saved in the session so
      // the user can be redirected back after login
      ctx.sessionAttribute("loginRedirect", ctx.path())
      ctx.redirect(Web.Uri.LOGIN)
    }
    else ctx.renderLatin1(Web.Template.UNAUTHORIZED, baseModel(ctx))
  }

  val internalError: Handler = { ctx ->
    val matchedPath = ctx.matchedPath()
    println("Internal server error with: $matchedPath")
    val model = baseModel(ctx)
    model["matchedPath"] = matchedPath
    ctx.renderLatin1(Web.Template.ERROR, model)
  }

  // Locale change can be initiated from any page
  // The locale is extracted from the request and saved to the user's session
  var handleLocaleChange: Handler = { ctx ->
    val locale = ctx.queryParam("locale")
    if (locale != null) {
      ctx.sessionAttribute("locale", locale)
      ctx.redirect(ctx.path())
    }
  }
}

class MessageBundle(languageTag: String?) {

  private val messages: ResourceBundle

  init {
    val locale = if (languageTag != null) Locale(languageTag) else Locale.ENGLISH
    this.messages = ResourceBundle.getBundle("localization/messages", locale)
  }
  operator fun get(message: String): String {
    return messages.getString(message)
  }
  operator fun get(key: String, vararg args: Any): String {
    return MessageFormat.format(get(key), *args)
  }
}

object LoginController {

  val serveLoginPage: Handler = { ctx ->
    val model = Web.baseModel(ctx)

    val loggedOut = ctx.sessionAttribute<String?>("loggedOut")
    ctx.sessionAttribute("loggedOut", null)
    model["loggedOut"] = (loggedOut != null)

    val loginRedirect = ctx.sessionAttribute<String?>("loginRedirect")
    ctx.sessionAttribute("loginRedirect", null)
    model["loginRedirect"] = loginRedirect

    ctx.renderLatin1(Web.Template.LOGIN, model)
  }

  val handleLoginPost: (UserDao) -> Handler = { userDao ->
    { ctx ->
      val model = Web.baseModel(ctx)

      val loginRedirect = ctx.formParam("loginRedirect")
      val user = userDao.authenticate(ctx.formParam("username"), ctx.formParam("password"))

      if (user == null) {
        model["authenticationFailed"] = true
        model["loginRedirect"] = loginRedirect
        ctx.renderLatin1(Web.Template.LOGIN, model)
      }
      else {
        ctx.sessionAttribute("curUser", user)
        model["currentUser"] = user.name
        model["currentRole"] = user.role
        if (loginRedirect != null) ctx.redirect(loginRedirect)
        else {
          model["authenticationSucceeded"] = true
          ctx.renderLatin1(Web.Template.LOGIN, model)
        }
      }
    }
  }

  // TODO Study if would it be better redirecting to INDEX, LOGIN or currentPage (the most complex option)
  val handleLogoutPost: Handler = { ctx ->
    ctx.sessionAttribute("curUser", null)
    ctx.sessionAttribute("loggedOut", "true")
    ctx.redirect(Web.Uri.LOGIN)
  }
}

object IndexController {
  val serveIndexPage: Handler = { ctx ->
    val model = Web.baseModel(ctx)
    ctx.renderLatin1(Web.Template.INDEX, model)
  }
}

object PagesController {
  val servePage1: Handler = { ctx ->
    // Populate model
    val model = Web.baseModel(ctx)
    // model["..."] = ... // call functions based on params (from ctx)
    model["books"] = bookDao.allBooks
    // Chose view and render it
    ctx.renderLatin1(Web.Template.PAGE1, model)
  }
}
