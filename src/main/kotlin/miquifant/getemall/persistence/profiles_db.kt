@file:JvmName("PersistenceProfiles")
/**
 * Persistence module with SQL statements and DB access functions for managing Profiles.
 *
 * Created by miquifant on 2020-12-28
 */
package miquifant.getemall.persistence

import miquifant.getemall.log.Loggable
import miquifant.getemall.model.Profile
import miquifant.getemall.utils.ConnectionManager


private object SQLprofile {

  val list = """
    |SELECT id, email, nickname, fullname, role, timestamp, verified, active
    |FROM users
    |ORDER BY id
  """.trimMargin().trim()

  val show = """
    |SELECT id, email, nickname, fullname, role, timestamp, verified, active
    |FROM users
    |WHERE id = ?
  """.trimMargin().trim()

  val showByName = """
    |SELECT id, email, nickname, fullname, role, timestamp, verified, active
    |FROM users
    |WHERE nickname = ?
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
          fullname = if (rs.getObject(4) != null) rs.getString(4) else rs.getString(3),
          role = rs.getInt(5),
          timestamp = rs.getTimestamp(6),
          verified = rs.getBoolean(7),
          active = rs.getBoolean(8)
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
          fullname = if (rs.getObject(4) != null) rs.getString(4) else rs.getString(3),
          role = rs.getInt(5),
          timestamp = rs.getTimestamp(6),
          verified = rs.getBoolean(7),
          active = rs.getBoolean(8)
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
          fullname = if (rs.getObject(4) != null) rs.getString(4) else rs.getString(3),
          role = rs.getInt(5),
          timestamp = rs.getTimestamp(6),
          verified = rs.getBoolean(7),
          active = rs.getBoolean(8)
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
