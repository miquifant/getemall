/**
 * Test functions of ServiceController.
 *
 * Created by miquifant on 2020-12-09
 */
package miquifant.getemall.controller

import miquifant.getemall.api.authentication.User
import miquifant.getemall.api.authentication.UserDao
import miquifant.getemall.api.authentication.impl.UserListDao
import miquifant.getemall.api.controller.ComponentStatus.DOWN
import miquifant.getemall.api.controller.ComponentStatus.OK
import miquifant.getemall.api.controller.ServiceController
import miquifant.getemall.log.Loggable.Logger
import miquifant.getemall.testingutils.initDatabaseConnection
import miquifant.getemall.testingutils.initUnreadyDatabaseConnection
import miquifant.getemall.utils.ConnectionManager

import org.junit.Test

import java.util.*

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNull


class TestServiceController {

  private lateinit var db: ConnectionManager
  private lateinit var unreadyDb: ConnectionManager
  private val tmpDatabase by lazy { "db_${Date().time}" }

  // Prepare a UserDao implementation which is never ready, by design
  object UnreadyUserDao: UserDao {
    override fun authenticate(username: String?, password: String?, accessLogger: Logger): User? = null
    override fun getUserByUsername(username: String): User? = throw RuntimeException("fail by design")
  }

  @BeforeTest
  fun setup() {
    db = initDatabaseConnection(tmpDatabase)
    unreadyDb = initUnreadyDatabaseConnection()
  }

  @AfterTest
  fun disposeDatabaseConnection() {
    db().close()
  }

  @Test
  fun testCheckLogin() {
    val checkReady = ServiceController.checkLogin(UserListDao())
    val checkUnready = ServiceController.checkLogin(UnreadyUserDao)

    assertEquals(OK, checkReady.status)
    assertNull(checkReady.message, "Message should be null")

    assertEquals(DOWN, checkUnready.status)
    assertEquals("Login component not ready", checkUnready.message)
  }

  @Test
  fun testCheckDB() {
    val checkReady = ServiceController.checkDB(db)
    val checkUnready = ServiceController.checkDB(unreadyDb)

    assertEquals(OK, checkReady.status)
    assertNull(checkReady.message, "Message should be null")

    assertEquals(DOWN, checkUnready.status)
    assertEquals("Database component not ready", checkUnready.message)
  }
}
