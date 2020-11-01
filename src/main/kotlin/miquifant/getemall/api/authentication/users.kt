/**
 * Users domain data class and Data access interface.
 *
 * Created by miquifant on 2020-10-25
 */
package miquifant.getemall.api.authentication

import miquifant.getemall.utils.AppRole


data class User(val name: String, val salt: String, val hashedPass: String, val role: AppRole)

/**
 * Data access object for Users
 */
interface UserDao {

  /**
   * Finds and returns a User by its name or returns null
   */
  fun getUserByUsername(username: String): User?

  /**
   * Checks username and password in the Users list and returns the matching one or null
   */
  fun authenticate(username: String?, password: String?): User?
}
