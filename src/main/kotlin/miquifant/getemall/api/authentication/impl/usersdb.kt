/**
 * Users Data access class, based on a database table.
 *
 * Created by miquifant on 2020-12-08
 */
package miquifant.getemall.api.authentication.impl

import miquifant.getemall.api.authentication.User
import miquifant.getemall.api.authentication.UserDao
import miquifant.getemall.log.Loggable
import miquifant.getemall.utils.AppRole
import miquifant.getemall.utils.ConnectionManager

import org.mindrot.jbcrypt.BCrypt


/**
 * Implements UserDao accessing a table called `users` which has fields:
 * - `nickname` (with an unique username)
 * - `salt`
 * - `password` (with hashed password, using previous salt)
 * - `role` (1: ADMIN, any other value including null: REGULAR_USER)
 * - `active` (true or record will be ignored)
 */
class UserDatabaseDao(val cm: ConnectionManager): UserDao {

  object DBAccessException: RuntimeException("Error accessing users database")

  /**
   * It only returns active users, and hides all info but its role.
   */
  private val queryUser = """
    |SELECT nickname, salt, password, role
    |FROM users
    |WHERE nickname = ?
    |  AND active = 1
  """.trimMargin().trim()

  /**
   * This implementation hides salt and hashed password to caller
   * @throws DBAccessException if a problem occurs while accessing the database
   */
  override fun getUserByUsername(username: String): User? {
    return try {
      val stmt = cm().prepareStatement(queryUser).apply {
        setString(1, username)
      }
      val rs = stmt.executeQuery()
      val ret = if (rs.next()) {
        val role = if (rs.getObject(4) != null) rs.getInt(4) else 0
        User(username, "", "", if (role == 1) AppRole.ADMIN else AppRole.REGULAR_USER)
      }
      else null
      stmt.closeOnCompletion()
      ret
    } catch (e: Exception) {
      // Technical (like connection error)
      // WE PURPOSELY DO NOT SHOW ERROR INFORMATION IN LOGS AND HIDE ACTUAL EXCEPTION TO CALLER!
      throw DBAccessException
    }
  }

  /**
   * Authenticate the user by hashing the inputted password using the stored salt,
   * then comparing the generated hashed password to the stored hashed password.
   * This implementation hides salt and hashed password to caller.
   * Inactive users cannot log in.
   * @throws DBAccessException if a problem occurs while accessing the database
   */
  override fun authenticate(username: String?, password: String?, accessLogger: Loggable.Logger): User? {
    return if (username == null || password == null) null
    else try {
      val stmt = cm().prepareStatement(queryUser).apply {
        setString(1, username)
      }
      val rs = stmt.executeQuery()
      val ret: User? = if (rs.next()) {
        val salt       = rs.getString(2)
        val hashedPass = rs.getString(3)
        if (BCrypt.hashpw(password, salt) == hashedPass) {
          val role = if (rs.getObject(4) != null) rs.getInt(4) else 0
          User(username, "", "", if (role == 1) AppRole.ADMIN else AppRole.REGULAR_USER)
        } else null
      }
      else null
      stmt.closeOnCompletion()
      if (ret == null) {
        accessLogger.info { "Login denied for username '$username'. Bad username or password" }
      }
      ret
    } catch (e: Exception) {
      // Technical (like connection error)
      // WE PURPOSELY DO NOT SHOW ERROR INFORMATION IN LOGS AND HIDE ACTUAL EXCEPTION TO CALLER!
      accessLogger.error { "Unable to authenticate '$username' due an error. ${DBAccessException.message}" }
      throw DBAccessException
    }
  }
}
