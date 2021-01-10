@file:JvmName("PersistenceProfiles")
/**
 * Persistence module with SQL statements and DB access functions for managing Profiles.
 *
 * Created by miquifant on 2020-12-28
 */
package miquifant.getemall.persistence

import miquifant.getemall.log.Loggable.Logger
import miquifant.getemall.model.Profile
import miquifant.getemall.model.ProfileExt
import miquifant.getemall.persistence.SQLReturnCode.Constraint
import miquifant.getemall.utils.ConnectionManager

import java.sql.SQLIntegrityConstraintViolationException


private object SQLprofile {

  val constraints: List<Constraint> = listOf(
      Constraint(Regex("(?i).*user_email_UN.*"), SQLReturnCode.UniqueError("Email already exists")),
      Constraint(Regex("(?i).*user_name_UN.*"), SQLReturnCode.UniqueError("Username already taken")),
      Constraint(Regex("(?i).*user_superpowers_FK.*"), SQLReturnCode.FKError("Invalid role"))
  )

  val list = """
    |SELECT id, email, nickname, role, timestamp, verified, active
    |FROM users
    |ORDER BY id
  """.trimMargin().trim()

  val show = """
    |SELECT u.id, u.email, u.nickname, u.role, u.timestamp, u.verified, u.active,
    |       p.profile_pic, p.full_name, p.pub_email, p.bio
    |FROM users u
    |  LEFT OUTER JOIN profiles p
    |    ON p.id = u.id
    |WHERE u.id = ?
  """.trimMargin().trim()

  val showByName = """
    |SELECT u.id, u.email, u.nickname, u.role, u.timestamp, u.verified, u.active,
    |       p.profile_pic, p.full_name, p.pub_email, p.bio
    |FROM users u
    |  LEFT OUTER JOIN profiles p
    |    ON p.id = u.id
    |WHERE u.nickname = ?
    |  AND active = true
  """.trimMargin().trim()

  val checkUsername = """
    |SELECT nickname
    |FROM users
    |WHERE nickname = ?
  """.trimMargin().trim()

  val patchUsername = """
    |UPDATE users
    |SET nickname = ?
    |WHERE nickname = ?
  """.trimMargin().trim()
}

fun retrieveProfilesList(db: ConnectionManager, logger: Logger):
    Pair<SQLReturnCode, List<Profile>>
{
  val ret = mutableListOf<Profile>()
  return try {
    val stmt = db().prepareStatement(SQLprofile.list)
    val rs = stmt.executeQuery()
    while (rs.next()) {
      ret.add(Profile(
          id = rs.getInt(1),
          email = rs.getString(2),
          name = rs.getString(3),
          role = rs.getInt(4),
          timestamp = rs.getTimestamp(5),
          verified = rs.getBoolean(6),
          active = rs.getBoolean(7)
      ))
    }
    stmt.closeOnCompletion()
    Pair(SQLReturnCode.Succeeded, ret)
  }
  // Technical (like connection error)
  catch (e: Exception) {
    val message = "Unable to recover profiles list due an internal error"
    logger.errorWithThrowable(e) { "$message: ${e.message}" }
    Pair(SQLReturnCode.DBError(message), ret)
  }
}

fun retrieveProfile(id: Int, db: ConnectionManager, logger: Logger):
    Pair<SQLReturnCode, List<Profile>>
{
  val ret = mutableListOf<Profile>()
  return try {
    val stmt = db().prepareStatement(SQLprofile.show).apply {
      setInt(1, id)
    }
    val rs = stmt.executeQuery()
    if (rs.next())
      ret.add(Profile(
          id = id,
          email = rs.getString(2),
          name = rs.getString(3),
          role = rs.getInt(4),
          timestamp = rs.getTimestamp(5),
          verified = rs.getBoolean(6),
          active = rs.getBoolean(7),
          ext = ProfileExt(
              profilePic = rs.getObject(8)?.toString(),
              fullName = rs.getObject(9)?.toString(),
              pubEmail = rs.getObject(10)?.toString(),
              bio = rs.getObject(11)?.toString()
          )
      ))
    stmt.closeOnCompletion()
    Pair(SQLReturnCode.Succeeded, ret)
  }
  // Technical (like connection error)
  catch (e: Exception) {
    val message = "Unable to recover profile id='$id' due an internal error"
    logger.errorWithThrowable(e) { "$message: ${e.message}" }
    Pair(SQLReturnCode.DBError(message), ret)
  }
}

fun retrieveProfile(name: String, db: ConnectionManager, logger: Logger):
    Pair<SQLReturnCode, List<Profile>>
{
  val ret = mutableListOf<Profile>()
  return try {
    val stmt = db().prepareStatement(SQLprofile.showByName).apply {
      setString(1, name)
    }
    val rs = stmt.executeQuery()
    if (rs.next())
      ret.add(Profile(
          id = rs.getInt(1),
          email = rs.getString(2),
          name = name,
          role = rs.getInt(4),
          timestamp = rs.getTimestamp(5),
          verified = rs.getBoolean(6),
          active = rs.getBoolean(7),
          ext = ProfileExt(
              profilePic = rs.getObject(8)?.toString(),
              fullName = rs.getObject(9)?.toString(),
              pubEmail = rs.getObject(10)?.toString(),
              bio = rs.getObject(11)?.toString()
          )
      ))
    stmt.closeOnCompletion()
    Pair(SQLReturnCode.Succeeded, ret)
  }
  // Technical (like connection error)
  catch (e: Exception) {
    val message = "Unable to recover profile '$name' due an internal error"
    logger.errorWithThrowable(e) { "$message: ${e.message}" }
    Pair(SQLReturnCode.DBError(message), ret)
  }
}

fun checkUsernameAvailability(username: String, db:ConnectionManager, logger: Logger): Pair<SQLReturnCode, Boolean> =
    try {
      val stmt = db().prepareStatement(SQLprofile.checkUsername).apply {
        setString(1, username)
      }
      val ret = !stmt.executeQuery().next()
      stmt.closeOnCompletion()
      Pair(SQLReturnCode.Succeeded, ret)
    }
    // Technical (like connection error)
    catch (e: Exception) {
      val message = "Unable to check Username availability due an internal error"
      logger.errorWithThrowable(e) { "$message: ${e.message}" }
      Pair(SQLReturnCode.DBError(message), false)
    }

fun patchProfileUsername(curName: String, newName: String, db: ConnectionManager, logger: Logger):
    SQLReturnCode =
    try {
      val stmt = db().prepareStatement(SQLprofile.patchUsername).apply {
        setString(1, newName)
        setString(2, curName)
      }
      val rows = stmt.executeUpdate()
      stmt.closeOnCompletion()
      if (rows == 1)

        SQLReturnCode.Patched

      else {
        logger.info { "Couldn't find profile to patch" }
        SQLReturnCode.NotFound
      }
    }
    // Functional (like name already taken)
    catch (e: SQLIntegrityConstraintViolationException) {
      val constraintError = SQLReturnCode.constraintErrorFromMessage(e.message, SQLprofile.constraints)
      logger.error {
        "Unable to patch profile '$curName' due an error: ${constraintError.message}"
      }
      constraintError
    }
    // Technical (like connection error)
    catch (e: Exception) {
      val message = "Unable to patch profile '$curName' due an internal error"
      logger.errorWithThrowable(e) { "$message: ${e.message}" }
      SQLReturnCode.DBError(message)
    }
