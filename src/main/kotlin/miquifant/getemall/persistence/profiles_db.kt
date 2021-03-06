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
      Constraint(Regex("(?i).*user_superpowers_FK.*"), SQLReturnCode.FKError("Invalid role")),
      Constraint(Regex("(?i).*profile_user_FK.*"), SQLReturnCode.FKError("Invalid user")),
      Constraint(Regex(".*PRIMARY.*"), SQLReturnCode.PKError("Profile already exists"))
  )

  val list = """
    |SELECT id, email, nickname, role, timestamp, verified, active
    |FROM users
    |ORDER BY id
  """.trimMargin().trim()

  val show = """
    |SELECT u.id, u.email, u.nickname, u.role, u.timestamp, u.verified, u.active,
    |       p.profile_pic, p.full_name, p.pub_email, p.pub_email_v, p.bio
    |FROM users u
    |  LEFT OUTER JOIN profiles p
    |    ON p.id = u.id
    |WHERE u.id = ?
  """.trimMargin().trim()

  val showByName = """
    |SELECT u.id, u.email, u.nickname, u.role, u.timestamp, u.verified, u.active,
    |       p.profile_pic, p.full_name, p.pub_email, p.pub_email_v, p.bio
    |FROM users u
    |  LEFT OUTER JOIN profiles p
    |    ON p.id = u.id
    |WHERE u.nickname = ?
    |  AND active = true
  """.trimMargin().trim()

  val checkId = """
    |SELECT 1
    |FROM users
    |WHERE id = ?
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

  // Warn: Use this query only for upserting purposes.
  // The rest of the cases use show to get only active accounts.
  val getExt = """
    |SELECT id,
    |       profile_pic, full_name, pub_email, pub_email_v, bio,
    |       timestamp
    |FROM profiles
    |WHERE id = ?
  """.trimMargin().trim()

  val updateExt = """
    |UPDATE profiles SET
    |  profile_pic = ?,
    |  full_name = ?,
    |  pub_email = ?,
    |  pub_email_v = ?,
    |  bio = ?,
    |  timestamp = CURRENT_TIMESTAMP
    |WHERE id = ?
  """.trimMargin().trim()

  val insertExt = """
    |INSERT INTO profiles (
    |  id, profile_pic, full_name, pub_email, pub_email_v, bio, timestamp
    |) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
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
              pubEmailVerified = rs.getBoolean(11),
              bio = rs.getObject(12)?.toString()
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
              pubEmailVerified = rs.getBoolean(11),
              bio = rs.getObject(12)?.toString()
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

fun checkIdExistence(id: Int, db:ConnectionManager, logger: Logger): Pair<SQLReturnCode, Boolean> =
    try {
      val stmt = db().prepareStatement(SQLprofile.checkId).apply {
        setInt(1, id)
      }
      val ret = stmt.executeQuery().next()
      stmt.closeOnCompletion()
      Pair(SQLReturnCode.Succeeded, ret)
    }
    // Technical (like connection error)
    catch (e: Exception) {
      val message = "Unable to check id existence due an internal error"
      logger.errorWithThrowable(e) { "$message: ${e.message}" }
      Pair(SQLReturnCode.DBError(message), false)
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

fun insertProfileExt(id: Int, ext: ProfileExt, db: ConnectionManager, logger: Logger):
    SQLReturnCode =
    try {
      val stmt = db().prepareStatement(SQLprofile.insertExt).apply {
        setInt(1, id)
        setString(2, ext.profilePic)
        setString(3, ext.fullName)
        setString(4, ext.pubEmail)
        setBoolean(5, ext.pubEmailVerified)
        setString(6, ext.bio)
      }
      val rows = stmt.executeUpdate()
      stmt.closeOnCompletion()
      if (rows > 0)

        SQLReturnCode.Inserted

      else {
        val message = "Unknown problem while persisting profile ext"
        logger.error { message }
        SQLReturnCode.error(message)
      }
    }
    // Functional (like profile already exists or invalid user: id must exist in users but cannot exist in profiles)
    catch (e: SQLIntegrityConstraintViolationException) {
      val constraintError = SQLReturnCode.constraintErrorFromMessage(e.message, SQLprofile.constraints)
      logger.error {
        "Unable to persist profile ext id='$id' due an error: ${constraintError.message}"
      }
      constraintError
    }
    // Technical (like connection error)
    catch (e: Exception) {
      val message = "Unable to persist profile ext id='$id' due an internal error"
      logger.errorWithThrowable(e) { "$message: ${e.message}" }
      SQLReturnCode.DBError(message)
    }

fun updateProfileExt(id: Int, ext: ProfileExt, db: ConnectionManager, logger: Logger):
    SQLReturnCode =
    try {
      val stmt = db().prepareStatement(SQLprofile.updateExt).apply {
        setString(1, ext.profilePic)
        setString(2, ext.fullName)
        setString(3, ext.pubEmail)
        setBoolean(4, ext.pubEmailVerified)
        setString(5, ext.bio)
        setInt(6, id)
      }
      val rows = stmt.executeUpdate()
      stmt.closeOnCompletion()
      if (rows == 1)

        SQLReturnCode.Updated

      else {
        logger.info { "Couldn't find profile to update" }
        SQLReturnCode.NotFound
      }
    }
    // Functional (like name already taken)
    catch (e: SQLIntegrityConstraintViolationException) {
      val constraintError = SQLReturnCode.constraintErrorFromMessage(e.message, SQLprofile.constraints)
      logger.error {
        "Unable to update profile ext id='$id' due an error: ${constraintError.message}"
      }
      constraintError
    }
    // Technical (like connection error)
    catch (e: Exception) {
      val message = "Unable to update profile ext id='$id' due an internal error"
      logger.errorWithThrowable(e) { "$message: ${e.message}" }
      SQLReturnCode.DBError(message)
    }

/**
 * This function is intentionally not visible, and it should be used ONLY for upserting purposes.
 * To retrieve the public profile of a user use retrieveProfile and extract the `ext` attribute.
 * It is important since that other function joins with users and filters inactive accounts.
 */
private fun retrieveProfileExt(id: Int, db: ConnectionManager, logger: Logger):
    Pair<SQLReturnCode, List<ProfileExt?>>
{
  val ret = mutableListOf<ProfileExt>()
  return try {
    val stmt = db().prepareStatement(SQLprofile.getExt).apply {
      setInt(1, id)
    }
    val rs = stmt.executeQuery()
    if (rs.next())
      ret.add(ProfileExt(
          profilePic = rs.getObject(2)?.toString(),
          fullName = rs.getObject(3)?.toString(),
          pubEmail = rs.getObject(4)?.toString(),
          pubEmailVerified = rs.getBoolean(5),
          bio = rs.getObject(6)?.toString()
      ))
    stmt.closeOnCompletion()
    Pair(SQLReturnCode.Succeeded, ret)
  }
  // Technical (like connection error)
  catch (e: Exception) {
    val message = "Unable to recover profile ext id='$id' due an internal error"
    logger.errorWithThrowable(e) { "$message: ${e.message}" }
    Pair(SQLReturnCode.DBError(message), ret)
  }
}

/**
 * Inserts or updates requested profile extension, returning old instance if existed
 */
fun upsertProfileExt(id: Int, ext: ProfileExt, db: ConnectionManager, logger: Logger):
    Pair<SQLReturnCode, ProfileExt?>
{
  val (queryCheckResult, accountExists) = checkIdExistence(id, db, logger)
  val checkSucceeded = queryCheckResult == SQLReturnCode.Succeeded
  return when {
    checkSucceeded && accountExists -> {
      val (queryProfileResult, oldExt) = retrieveProfileExt(id, db, logger)
      val queryProfileSucceeded = queryProfileResult == SQLReturnCode.Succeeded
      val extExisted = queryProfileSucceeded && oldExt.isNotEmpty()
      val retExt: ProfileExt? = if (extExisted) oldExt[0] else null
      when {
        queryProfileSucceeded && extExisted && ext == retExt -> Pair(SQLReturnCode.Unaltered, retExt)
        queryProfileSucceeded && extExisted -> Pair(updateProfileExt(id, ext, db, logger), retExt)
        queryProfileSucceeded -> Pair(insertProfileExt(id, ext, db, logger), retExt)
        // Technical (like connection error)
        else -> {
          val message = "Unable to update profile due an internal error"
          logger.error { message }
          Pair(SQLReturnCode.DBError(message), retExt)
        }
      }
    }
    checkSucceeded -> {
      logger.info { "Couldn't find account to update" }
      Pair(SQLReturnCode.NotFound, null)
    }
    // Technical (like connection error)
    else -> {
      val message = "Unable to update profile due an internal error"
      logger.error { message }
      Pair(SQLReturnCode.DBError(message), null)
    }
  }
}
