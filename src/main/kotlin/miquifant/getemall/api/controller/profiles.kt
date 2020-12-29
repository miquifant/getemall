/**
 * URIs and Handlers for application profiles.
 *
 * Created by miquifant on 2020-12-24
 */
package miquifant.getemall.api.controller

import miquifant.getemall.log.LoggerFactory
import miquifant.getemall.model.ExceptionalResponse
import miquifant.getemall.persistence.*
import miquifant.getemall.utils.ConnectionManager
import miquifant.getemall.utils.Handler
import miquifant.getemall.utils.exception


object Profiles {
  object Uri {
    const val PROFILES        = "/api/profiles"
    const val PROFILE_BY_NAME = "/api/profiles/name/:name"
    const val ONE_PROFILE     = "/api/profiles/:id"
  }
}

object ProfilesController {

  private val logger = LoggerFactory.logger(this::class.java.canonicalName)

  val getAll: (ConnectionManager) -> Handler = { db ->
    { ctx ->
      val (ret, profiles) = retrieveProfilesList(db, logger)
      when (ret) {

        // 200 Ok
        SQLReturnCode.Succeeded -> ctx.json(profiles)

        // 503 Service Unavailable: If a technical error occurred "Service unavailable"
        else -> ctx.exception(ExceptionalResponse.fromSQLReturnCode(ret))
      }
    }
  }

  val getOne: (ConnectionManager) -> Handler = { db ->
    { ctx ->
      val id = try { ctx.pathParam<Int>("id").getOrNull() } catch (e: Exception) { null }
      if (id != null) {
        val (ret, profiles) = retrieveProfile(id, db, logger)
        when {

          // 200 Ok
          ret == SQLReturnCode.Succeeded && profiles.isNotEmpty() -> ctx.json(profiles[0])

          // 404 Not found
          profiles.isEmpty() -> ctx.status(404)

          // 503 Service Unavailable: If a technical error occurred "Service unavailable"
          else -> ctx.exception(ExceptionalResponse.fromSQLReturnCode(ret))
        }
      }
      // 400 Bad request
      else ctx.exception(400, "Wrong id '${ctx.pathParam("id")}'. It should be an integer number")
    }
  }

  val getByName: (ConnectionManager) -> Handler = { db ->
    { ctx ->
      val name = ctx.pathParam<String>("name").get()
      val (ret, profiles) = retrieveProfile(name, db, logger)
      when {

        // 200 Ok
        ret == SQLReturnCode.Succeeded && profiles.isNotEmpty() -> ctx.json(profiles[0])

        // 404 Not found
        profiles.isEmpty() -> ctx.status(404)

        // 503 Service Unavailable: If a technical error occurred "Service unavailable"
        else -> ctx.exception(ExceptionalResponse.fromSQLReturnCode(ret))
      }
    }
  }
}
