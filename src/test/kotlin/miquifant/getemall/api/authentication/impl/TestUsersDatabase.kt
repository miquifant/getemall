/**
 * Test usersdb implementation of UserDao.
 *
 * Created by miquifant on 2020-12-08
 */
package miquifant.getemall.api.authentication.impl

import miquifant.getemall.log.Loggable.Logger
import miquifant.getemall.log.LoggerFactory
import miquifant.getemall.testingutils.initDatabaseConnection
import miquifant.getemall.testingutils.initUnreadyDatabaseConnection
import miquifant.getemall.utils.AppRole
import miquifant.getemall.utils.ConnectionManager

import java.util.*

import kotlin.test.*


class TestUsersDatabase {

  private lateinit var logger: Logger
  private lateinit var db: ConnectionManager
  private lateinit var unreadyDb: ConnectionManager
  private val tmpDatabase by lazy { "db_${Date().time}" }

  @BeforeTest
  fun setup() {
    logger = LoggerFactory.logger(this::class.java.canonicalName)
    db = initDatabaseConnection(tmpDatabase)
    unreadyDb = initUnreadyDatabaseConnection()
  }

  @AfterTest
  fun disposeDatabaseConnection() {
    db().close()
  }

  @Test
  fun testGetUserByUsername() {

    val unexistingName = "unexisting"

    val existingName1 = "miqui"
    val existingRole1 = AppRole.ADMIN

    val existingName2 = "esther"
    val existingRole2 = AppRole.REGULAR_USER

    val usersDao = UserDatabaseDao(db)
    val unreadyUsersDao = UserDatabaseDao(unreadyDb)

    val unexistingUser = try {
      usersDao.getUserByUsername(unexistingName)
    } catch (e: Exception) {
      fail("method shouldn't throw an exception")
    }
    assertNull(unexistingUser, "User '$unexistingName' shouldn't be found in database")

    val existingUser1 = try {
      usersDao.getUserByUsername(existingName1)
    } catch (e: Exception) {
      fail("method shouldn't throw an exception")
    }
    assertNotNull(existingUser1, "User '$existingName1' should be found in database")
    assertEquals(existingName1, existingUser1.name)
    assertEquals("", existingUser1.salt)
    assertEquals("", existingUser1.hashedPass)
    assertEquals(existingRole1, existingUser1.role)

    val existingUser2 = try {
      usersDao.getUserByUsername(existingName2)
    } catch (e: Exception) {
      fail("method shouldn't throw an exception")
    }
    assertNotNull(existingUser2, "User '$existingName2' should be found in database")
    assertEquals(existingName2, existingUser2.name)
    assertEquals("", existingUser2.salt)
    assertEquals("", existingUser2.hashedPass)
    assertEquals(existingRole2, existingUser2.role)

    val exception = try {
      unreadyUsersDao.getUserByUsername(existingName2)
      fail("method should throw an exception")
    } catch (e: Exception) {
      e
    }
    assertNotNull(exception, "Method should throw an exception")
  }

  @Test
  fun testAuthenticate() {

    val unexistingName = "unexisting"
    val wrongPassword  = "wrong"

    val existingName1  = "miqui"
    val rightPassword1 = "password"
    val existingRole1  = AppRole.ADMIN

    val existingName2  = "esther"
    val rightPassword2 = "password"
    val existingRole2  = AppRole.REGULAR_USER

    val usersDao = UserDatabaseDao(db)
    val unreadyUsersDao = UserDatabaseDao(unreadyDb)

    val unexistingUser = try {
      usersDao.authenticate(unexistingName, "whatever", logger)
    } catch (e: Exception) {
      fail("authentication shouldn't throw exception")
    }
    assertNull(unexistingUser, "User shouldn't be authenticated")

    val userWithWrongPassword = try {
      usersDao.authenticate(existingName1, wrongPassword, logger)
    } catch (e: Exception) {
      fail("authentication shouldn't throw exception")
    }
    assertNull(userWithWrongPassword, "User shouldn't be authenticated")

    val user1 = try {
      usersDao.authenticate(existingName1, rightPassword1, logger)
    } catch (e: Exception) {
      fail("authentication shouldn't throw exception")
    }
    assertNotNull(user1, "User should authenticate")
    assertEquals(existingName1, user1.name)
    assertEquals("", user1.salt)
    assertEquals("", user1.hashedPass)
    assertEquals(existingRole1, user1.role)

    val user2 = try {
      usersDao.authenticate(existingName2, rightPassword2, logger)
    } catch (e: Exception) {
      fail("authentication shouldn't throw exception")
    }
    assertNotNull(user2, "User should authenticate")
    assertEquals(existingName2, user2.name)
    assertEquals("", user2.salt)
    assertEquals("", user2.hashedPass)
    assertEquals(existingRole2, user2.role)

    val exception = try {
      unreadyUsersDao.authenticate(existingName2, rightPassword2, logger)
      fail("method should throw an exception")
    } catch (e: Exception) {
      e
    }
    assertNotNull(exception, "Method should throw an exception")
  }
}
