/**
 * Test functions of ProfilesController.
 *
 * Created by miquifant on 2021-01-09
 */
package miquifant.getemall.controller

import miquifant.getemall.api.controller.ProfilesController

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class TestProfilesController {

  @Test
  fun testIsValidUsername() {

    val validName = "valid-name-1"

    val tooShort = "x"
    val tooLong = "12345678101234567820123456783012345678401"
    val startHyphen = "-invalid"
    val endHyphen = "invalid-"
    val doubleHyphen = "invalid--name"
    val invalidChars = "invalid.name"

    assertTrue(ProfilesController.isValidUsername(validName), "username should be valid")

    assertFalse(ProfilesController.isValidUsername(tooShort), "username shouldn't be valid")
    assertFalse(ProfilesController.isValidUsername(tooLong), "username shouldn't be valid")
    assertFalse(ProfilesController.isValidUsername(startHyphen), "username shouldn't be valid")
    assertFalse(ProfilesController.isValidUsername(endHyphen), "username shouldn't be valid")
    assertFalse(ProfilesController.isValidUsername(doubleHyphen), "username shouldn't be valid")
    assertFalse(ProfilesController.isValidUsername(invalidChars), "username shouldn't be valid")
  }
}
