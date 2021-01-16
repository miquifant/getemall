/**
 * Test functions of ProfilesController.
 *
 * Created by miquifant on 2021-01-09
 */
package miquifant.getemall.controller

import miquifant.getemall.api.controller.ProfilesController
import miquifant.getemall.model.ProfileExt
import miquifant.getemall.utils.toSingleLine

import kotlin.test.Test
import kotlin.test.assertEquals
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

  @Test
  fun testIsValidEmail() {
    val validEmail = "miqui@personal.fake"
    val validExtremeEmail = "Chars+Nums.1-Mixed_Symbols%AndThat@justchars.and.nums.000.and.dots"
    val nonValid1 = "nameAtDomain"      // missing '@'
    val nonValid2 = "miqui@españa.com"  // invalid char 'ñ'

    assertTrue(ProfilesController.isValidEmail(validEmail), "email should be valid")
    assertTrue(ProfilesController.isValidEmail(validExtremeEmail), "email should be valid")
    assertFalse(ProfilesController.isValidEmail(nonValid1), "email should not be valid")
    assertFalse(ProfilesController.isValidEmail(nonValid2), "email should not be valid")
  }

  @Test
  fun testIsValidExt() {
    val emptyProfileExt = ProfileExt()

    val validProfileExt = ProfileExt (
        profilePic = "shorter_than_128_chars_name.png",
        fullName = "Short Enough Name",
        pubEmail = "valid-address@some.domain",
        bio = "Bio with a valid text no longer than 256 characters, which is the max allowed length"
    )
    val nonValidProfileExt = ProfileExt (
        //            1234567 10 234567 20 234567 30 234567 40 234567 50 234567 60 234567 70 234567 80 234567 90 23456 100 23456 110 23456 120 23456 130
        //           |    ·    |    ·    |    ·    |    ·    |    ·    |    ·    |    ·    |    ·    |    ·    |    ·    |    ·    |    ·    |    ·    |
        profilePic = "too_long_name_____20_234567_30_234567_40_234567_50_234567_60_234567_70_234567_80_234567_90_23456_100_23456_110_23456_120_23456789",
        fullName   = "Too Long Fullname 20 234567_30_234567_40_234567_50_234567_60_234567_70_234567_80_234567_90_23456_100_23456_110_23456_120_23456789",
        pubEmail   = "invalid",
        bio        = """
          |Too long biography, exceeding 256 characters 67 50 234567 and 64
          |1234567 10 234567 20 234567 30 234567 40 234567 50 23456 and 128
          |1234567 10 234567 20 234567 30 234567 40 234567 50 23456 and 192
          |1234567 10 234567 20 234567 30 234567 40 234567 50 234567 and 257
        """.trimMargin().trim().toSingleLine()
    )

    val errEmptyProfileExt = ProfilesController.validateExt(emptyProfileExt)
    val errValidProfileExt = ProfilesController.validateExt(validProfileExt)
    val errNonValidProfileExt = ProfilesController.validateExt(nonValidProfileExt)

    assertEquals(0, errEmptyProfileExt.size)

    assertEquals(0, errValidProfileExt.size)

    assertEquals(4, errNonValidProfileExt.size)
    assertEquals("Profile pic name cannot exceed 128 characters", errNonValidProfileExt[0])
    assertEquals("Name cannot exceed 128 characters", errNonValidProfileExt[1])
    assertEquals("Email must be a valid address and cannot exceed 128 characters", errNonValidProfileExt[2])
    assertEquals("Bio cannot exceed 256 characters", errNonValidProfileExt[3])
  }
}
