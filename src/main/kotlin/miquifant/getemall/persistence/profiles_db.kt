@file:JvmName("PersistenceProfiles")
/**
 * Persistence module with SQL statements and DB access functions for managing Profiles.
 *
 * Created by miquifant on 2020-12-28
 */
package miquifant.getemall.persistence

import miquifant.getemall.log.Loggable
import miquifant.getemall.model.Profile
import miquifant.getemall.model.ProfileExt
import miquifant.getemall.utils.ConnectionManager


private object SQLprofile {

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
}

fun retrieveProfilesList(db: ConnectionManager, logger: Loggable.Logger):
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

fun retrieveProfile(id: Int, db: ConnectionManager, logger: Loggable.Logger):
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

fun retrieveProfile(name: String, db: ConnectionManager, logger: Loggable.Logger):
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
