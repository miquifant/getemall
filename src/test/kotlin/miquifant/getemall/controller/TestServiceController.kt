package miquifant.getemall.controller

import miquifant.getemall.api.authentication.User
import miquifant.getemall.api.authentication.UserDao
import miquifant.getemall.api.authentication.impl.UserListDao
import miquifant.getemall.api.controller.ComponentCheck
import miquifant.getemall.api.controller.ServiceController
import miquifant.getemall.log.Loggable
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TestServiceController {

  // Prepare a UserDao implementation which is never ready, by design
  object UnreadyUserDao: UserDao {
    override fun authenticate(username: String?, password: String?, accessLogger: Loggable.Logger): User? = null
    override fun getUserByUsername(username: String): User? = throw RuntimeException("fail by design")
  }

  @Test
  fun testCheckLogin() {
    val checkReady = ServiceController.checkLogin(UserListDao())
    val checkUnready = ServiceController.checkLogin(UnreadyUserDao)

    assertEquals(ComponentCheck.ComponentStatus.OK, checkReady.status)
    assertNull(checkReady.message, "Message should be null")

    assertEquals(ComponentCheck.ComponentStatus.DOWN, checkUnready.status)
    assertEquals("Login component not ready", checkUnready.message)
  }
}
