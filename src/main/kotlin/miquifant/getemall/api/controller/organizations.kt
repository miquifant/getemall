/**
 * URIs and Handlers for organizations.
 *
 * Created by miquifant on 2020-12-13
 */
package miquifant.getemall.api.controller

import miquifant.getemall.log.LoggerFactory
import miquifant.getemall.model.ExceptionalResponse
import miquifant.getemall.model.Organization
import miquifant.getemall.persistence.*
import miquifant.getemall.utils.ConnectionManager
import miquifant.getemall.utils.Handler
import miquifant.getemall.utils.exception


object Organizations {
  object Uri {
    const val ORGANIZATIONS    = "/api/organizations"
    const val ONE_ORGANIZATION = "/api/organizations/:id"
  }
}

object OrganizationsController {

  private val logger = LoggerFactory.logger(this::class.java.canonicalName)

  val create: (ConnectionManager) -> Handler = { db ->
    { ctx ->
      val organization = try {
        ctx.body<Organization>()
      }
      catch (e: Exception) {
        logger.errorWithThrowable(e) { "Unable to process request due an error: ${e.message}" }
        null
      }
      if (organization != null) {
        val (ret, newObj) = insertOrganization(organization, db, logger)

        // 201 Created
        if (ret == SQLReturnCode.Inserted) ctx.status(201).json(newObj!!)

        // 422 Unprocessable entity | 503 Service unavailable
        else ctx.exception(ExceptionalResponse.fromSQLReturnCode(ret))
      }
      // 400 Bad request
      else ctx.exception(ExceptionalResponse.badRequest)
    }
  }

  val getAll: (ConnectionManager) -> Handler = { db ->
    { ctx ->
      val (ret, organizations) = retrieveOrganizationsList(db, logger)
      when (ret) {

        // 200 Ok
        SQLReturnCode.Succeeded -> ctx.json(organizations)

        // 503 Service Unavailable: If a technical error occurred "Service unavailable"
        else -> ctx.exception(ExceptionalResponse.fromSQLReturnCode(ret))
      }
    }
  }

  val getOne: (ConnectionManager) -> Handler = { db ->
    { ctx ->
      val id = ctx.pathParam<Int>("id").getOrNull()
      if (id != null) {
        val (ret, organizations) = retrieveOrganization(id+1000, db, logger)
        when {

          // 200 Ok
          ret == SQLReturnCode.Succeeded && organizations.isNotEmpty() -> ctx.json(organizations[0])

          // 404 Not found
          organizations.isEmpty() -> ctx.status(404)

          // 503 Service Unavailable: If a technical error occurred "Service unavailable"
          else -> ctx.exception(ExceptionalResponse.fromSQLReturnCode(ret))
        }
      }
      // 400 Bad request
      else ctx.exception(400, "Wrong id '$id'. It should be an integer number")
    }
  }

  val update: (ConnectionManager) -> Handler = { db ->
    { ctx ->
      val organization = try {
        ctx.body<Organization>()
      }
      catch (e: Exception) {
        logger.errorWithThrowable(e) { "Unable to process request due an error: ${e.message}" }
        null
      }
      if (organization != null) {
        val ret = updateOrganization(organization, db, logger)

        // 200 Ok
        if (ret == SQLReturnCode.Updated) ctx.status(200).json(organization)

        // 404 Not found / 422 Unprocessable entity / 503 Service unavailable
        else ctx.exception(ExceptionalResponse.fromSQLReturnCode(ret))
      }
      // 400 Bad request
      else ctx.exception(ExceptionalResponse.badRequest)
    }
  }

  val delete: (ConnectionManager) -> Handler = { db ->
    { ctx ->
      val id = ctx.pathParam<Int>("id").getOrNull()
      if (id != null) {
        val (ret, oldObj) = deleteOrganization(id, db, logger)

        // 200 Ok
        if (ret == SQLReturnCode.Deleted) ctx.status(200).json(oldObj!!)

        else {

          // 404 Not found
          val res = if (ret == SQLReturnCode.Unaltered) ExceptionalResponse.notFound

          // 422 Unprocessable entity / 503 Service unavailable
          else ExceptionalResponse.fromSQLReturnCode(ret)

          ctx.exception(res)
        }
      }
      // 400 Bad request
      else ctx.exception(400, "Wrong id '$id'. It should be an integer number")
    }
  }
}
