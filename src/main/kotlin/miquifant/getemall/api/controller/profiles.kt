/**
 * URIs and Handlers for application profiles.
 *
 * Created by miquifant on 2020-12-24
 */
package miquifant.getemall.api.controller

import miquifant.getemall.api.authentication.User
import miquifant.getemall.log.LoggerFactory
import miquifant.getemall.model.ExceptionalResponse
import miquifant.getemall.model.Patch
import miquifant.getemall.model.ProfileExt
import miquifant.getemall.persistence.*
import miquifant.getemall.utils.ConnectionManager
import miquifant.getemall.utils.Handler
import miquifant.getemall.utils.exception
import miquifant.getemall.utils.toSingleLine


object Profiles {
  object Uri {
    const val PROFILES        = "/api/profiles"
    const val OWN_PROFILE     = "/api/profiles/me"
    const val OWN_PUB_PROFILE = "/api/profiles/me/ext"
    const val PROFILE_BY_NAME = "/api/profiles/name/:name"
    const val ONE_PROFILE     = "/api/profiles/:id"
  }
}

object ProfilesController {

  private val logger = LoggerFactory.logger(this::class.java.canonicalName)

  private val validNameDescription: String = """
    |Username may only contain alphanumeric characters or single hyphens,
    |must be between 2 and 40 characters long, and cannot begin or end with a hyphen
    |""".trimMargin().trim().toSingleLine(" ")

  private val validNameStructure = "^[a-z0-9]([a-z0-9]|[-]){0,38}[a-z0-9]$".toRegex(RegexOption.IGNORE_CASE)
  private val hasTwoHyphensInARow = "[-][-]".toRegex()
  private val validEmailStructure = """
    |[a-z0-9\+\.\_\%\-]{1,124}
    |[@]
    |[a-z0-9][a-z0-9\-]{0,64}
    |([.][a-z0-9][a-z0-9\-]{0,25})+
    |""".trimMargin().toSingleLine().trim().toRegex(RegexOption.IGNORE_CASE)

  fun isValidUsername(name: String): Boolean =
      validNameStructure.matches(name) &&
          !hasTwoHyphensInARow.containsMatchIn(name)

  fun isValidEmail(email: String): Boolean = validEmailStructure.matches(email)

  fun validateExt(ext: ProfileExt): List<String> {
    val ret = mutableListOf<String>()

    if (ext.profilePic?.length ?: 0 > 128)
      ret.add("Profile pic name cannot exceed 128 characters")

    if (ext.fullName?.length ?: 0 > 128)
      ret.add("Name cannot exceed 128 characters")

    if (ext.pubEmail != null && (ext.pubEmail.length > 128 || !isValidEmail(ext.pubEmail)))
      ret.add("Email must be a valid address and cannot exceed 128 characters")

    if (ext.bio?.length ?: 0 > 256)
      ret.add("Bio cannot exceed 256 characters")

    return ret
  }

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

  val getOwn: (ConnectionManager) -> Handler = { db ->
    { ctx ->
      val name = ctx.sessionAttribute<User?>("curUser")?.name
      if (name != null) {
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
      // 401 Unauthorized
      else ctx.exception(ExceptionalResponse.unauthorized)
    }
  }

  val checkUsername: (ConnectionManager) -> Handler = { db ->
    { ctx ->
      val name = ctx.pathParam<String>("name").get()
      val (ret, available) = checkUsernameAvailability(name, db, logger)
      when {

        // 404 Not found (username is available)
        ret == SQLReturnCode.Succeeded && available -> ctx.status(404)

        // 200 (username is not available)
        ret == SQLReturnCode.Succeeded -> ctx.status(200)

        // 503 Service Unavailable: If a technical error occurred "Service unavailable"
        else -> ctx.status(503)
      }
    }
  }

  val getPublicProfile: (ConnectionManager) -> Handler = { db ->
    { ctx ->
      val name = ctx.pathParam<String>("name").get()
      val (ret, profiles) = retrieveProfile(name, db, logger)
      when {

        // Some record was found, but we only show profiles after the email verification
        ret == SQLReturnCode.Succeeded && profiles.isNotEmpty() -> {

          // 200 Ok  -- we hide the id and the primary email
          if (profiles[0].verified) ctx.json(profiles[0].copy(id = 1, email = ""))

          // 404 Not found (profile is still in verification status, so it doesn't exist as public profile)
          else ctx.status(404)
        }
        // 404 Not found
        profiles.isEmpty() -> ctx.status(404)

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

  val patchOwn: (ConnectionManager) -> Handler = { db ->
    { ctx ->
      val currentUser = ctx.sessionAttribute<User?>("curUser")
      if (currentUser != null) {
        if (ctx.contentType() == "application/merge-patch+json") {
          val patch = try {
            ctx.body<Patch>()
          } catch (e: Exception) {
            logger.errorWithThrowable(e) { "Unable to process request due an error: ${e.message}" }
            null
          }
          if (patch != null) {
            // Analyze type of patch, because we allow just some patching on users
            when {
              // patch Username
              patch.size == 1 && patch.containsKey("name") -> {
                val newName = patch["name"]?.toString() ?: ""

                // 200 Ok (with no changes)
                if (newName == currentUser.name) ctx.status(204)

                else if (isValidUsername(newName)) {

                  val ret = patchProfileUsername(currentUser.name, newName, db, logger)

                  // 200 Ok
                  if (ret == SQLReturnCode.Patched) {
                    ctx.sessionAttribute("curUser", currentUser.copy(name = newName))
                    ctx.status(204)
                  }
                  // 404 Not found / 422 Unprocessable entity / 503 Service unavailable
                  else ctx.exception(ExceptionalResponse.fromSQLReturnCode(ret))
                }
                // 422 Unprocessable Entity (invalid username)
                else ctx.exception(422, validNameDescription)
              }
              // 422 Unprocessable Entity (unacceptable type of patch)
              else -> ctx.exception(422, "Unprocessable request")
            }
          }
          // 400 Bad request (Malformed patch document)
          else ctx.exception(ExceptionalResponse.badRequest)
        }
        // 415 Unsupported media type (Unsupported patch document)
        else ctx.exception(ExceptionalResponse.unsupportedMediaType)
      }
      // 401 Unauthorized
      else ctx.exception(ExceptionalResponse.unauthorized)
    }
  }

  val upsertOwnPubProfile: (ConnectionManager) -> Handler = { db ->
    { ctx ->
      val currentUser = ctx.sessionAttribute<User?>("curUser")
      if (currentUser != null) {
        val newExt = try {
          ctx.body<ProfileExt>()
        } catch (e: Exception) {
          logger.errorWithThrowable(e) { "Unable to process request due an error: ${e.message}" }
          null
        }
        if (newExt != null) {

          // retrieve profile extension from current user
          val (retGetProfile, profiles) = retrieveProfile(currentUser.name, db, logger)
          when {
            retGetProfile == SQLReturnCode.Succeeded && profiles.isNotEmpty() -> {

              val currentProfile = profiles[0]

              // 200 Ok (with no changes)
              if (newExt == currentProfile.ext) ctx.status(200).json(currentProfile.ext)

              // 409 Conflict (Tried to write a readonly attribute)
              else if (newExt.pubEmailVerified != currentProfile.ext.pubEmailVerified)
                ctx.exception(409, "Attempt to update a readonly attribute: 'pubEmailVerified'")

              else {
                val errors = validateExt(newExt)
                if (errors.isEmpty()) {

                  // If the pubEmail changes, and it's different from a validated primary email, then we force flag
                  // pubEmailVerified = false
                  val extToWrite =
                      if (newExt.pubEmail?.toLowerCase() == currentProfile.ext.pubEmail?.toLowerCase())
                        newExt
                      else if (newExt.pubEmail?.toLowerCase() == currentProfile.email.toLowerCase() &&
                          currentProfile.verified)
                        newExt.copy(pubEmailVerified = true)
                      else
                        newExt.copy(pubEmailVerified = false)

                  val ret = upsertProfileExt(currentProfile.id, extToWrite, db, logger).first

                  // 200 Ok
                  if (ret == SQLReturnCode.Updated || ret == SQLReturnCode.Unaltered) ctx.status(200).json(extToWrite)

                  // 201 Created
                  else if (ret == SQLReturnCode.Inserted) ctx.status(201).json(extToWrite)

                  // 404 Not found / 422 Unprocessable entity / 503 Service unavailable
                  else ctx.exception(ExceptionalResponse.fromSQLReturnCode(ret))
                }
                // 422 Unprocessable Entity (some invalid field)
                else {
                  ctx.exception(422, errors.joinToString(
                      separator = "\n",
                      prefix = "Some errors were found:\n",
                      postfix = "\nPlease fix them and try again.") {
                    "- $it"
                  })
                }
              }
            }
            // 404 Not found
            retGetProfile == SQLReturnCode.Succeeded -> ctx.exception(ExceptionalResponse.notFound)

            // 503 Service Unavailable: If a technical error occurred "Service unavailable"
            else -> ctx.exception(ExceptionalResponse.fromSQLReturnCode(retGetProfile))
          }
        }
        // 400 Bad request (Malformed profile document)
        else ctx.exception(ExceptionalResponse.badRequest)
      }
      // 401 Unauthorized
      else ctx.exception(ExceptionalResponse.unauthorized)
    }
  }
}
