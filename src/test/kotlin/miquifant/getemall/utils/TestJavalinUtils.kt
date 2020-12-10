/**
 * Test javalin utils functions.
 *
 * Created by miquifant on 2020-12-10
 */
package miquifant.getemall.utils

import miquifant.getemall.utils.AppRole.ADMIN
import miquifant.getemall.utils.AppRole.ANONYMOUS
import miquifant.getemall.utils.AppRole.REGULAR_USER

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class TestJavalinUtils {

  @Test
  fun testGrantedFor() {
    val adminsOnly = GrantedFor.admins
    assertEquals(1, adminsOnly.size)
    assertTrue(adminsOnly.contains(ADMIN), "permissions should contain ADMIN role")

    val loggedInUsers = GrantedFor.loggedInUsers
    assertEquals(2, loggedInUsers.size)
    assertTrue(loggedInUsers.contains(ADMIN), "permissions should contain ADMIN role")
    assertTrue(loggedInUsers.contains(REGULAR_USER), "permissions should contain REGULAR_USER role")

    val allUsers = GrantedFor.anyone
    assertEquals(3, allUsers.size)
    assertTrue(allUsers.contains(ADMIN), "permissions should contain ADMIN role")
    assertTrue(allUsers.contains(REGULAR_USER), "permissions should contain REGULAR_USER role")
    assertTrue(allUsers.contains(ANONYMOUS), "permissions should contain ANONYMOUS role")
  }
}
