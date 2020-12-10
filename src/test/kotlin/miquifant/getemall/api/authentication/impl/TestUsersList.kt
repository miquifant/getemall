/**
 * Test userslist implementation of UserDao.
 *
 * Created by miquifant on 2020-11-02
 */
package miquifant.getemall.api.authentication.impl

import miquifant.getemall.log.LoggerFactory
import miquifant.getemall.utils.AppRole

import org.mindrot.jbcrypt.BCrypt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull


class TestUsersList {

  private val logger = LoggerFactory.logger(this::class.java.canonicalName)

  @Test
  fun testGetUserByUsername() {

    val unexistingName = "unexisting"

    val existingName = "miqui"
    val existingPass = "password"
    val existingSalt = "$2a$10\$h.dl5J86rGH7I8bD9bZeZe"
    val existingRole = AppRole.ADMIN

    val usersDao = UserListDao()

    val unexistingUser = usersDao.getUserByUsername(unexistingName)
    assertNull(unexistingUser, "User '$unexistingName' shouldn't be found in list")

    val existingUser = usersDao.getUserByUsername(existingName)
    assertNotNull(existingUser, "User '$existingName' should be found in list")
    assertEquals(existingName, existingUser.name)
    assertEquals(existingSalt, existingUser.salt)
    assertEquals(BCrypt.hashpw(existingPass, existingUser.salt), existingUser.hashedPass)
    assertEquals(existingRole, existingUser.role)
  }

  @Test
  fun testAuthenticate() {

    val unexistingName = "unexisting"
    val wrongPassword = "wrong"

    val existingName  = "esther"
    val rightPassword = "password"
    val existingSalt  = "$2a$10\$e0MYzXyjpJS7Pd0RVvHwHe"
    val existingRole  = AppRole.REGULAR_USER

    val usersDao = UserListDao()

    val unexistingUser = usersDao.authenticate(unexistingName, "whatever", logger)
    assertNull(unexistingUser, "User shouldn't be authenticated")

    val userWithWrongPassword = usersDao.authenticate(existingName, wrongPassword, logger)
    assertNull(userWithWrongPassword, "User shouldn't be authenticated")

    val user = usersDao.authenticate(existingName, rightPassword, logger)
    assertNotNull(user, "User should authenticate")
    assertEquals(existingName, user.name)
    assertEquals(existingSalt, user.salt)
    assertEquals(BCrypt.hashpw(rightPassword, user.salt), user.hashedPass)
    assertEquals(existingRole, user.role)
  }
}
