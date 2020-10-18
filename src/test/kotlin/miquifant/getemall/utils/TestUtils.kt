/**
 * Test getemall-api utillity functions.
 *
 * Created by miquifant on 2020-10-18
 */
package miquifant.getemall.utils

import java.util.Date

import kotlin.test.Test
import kotlin.test.assertEquals


class TestUtils {

  @Test
  fun testFormatISO8601Date() {
    val zero = 0L
    val now = 1602991350000L
    assertEquals("1970-01-01 01:00:00", formatISO8601Date(Date(zero)))
    assertEquals("2020-10-18 05:22:30", formatISO8601Date(Date(now)))
  }

  @Test
  fun testRetrieveMetadata() {
    val metadata = retrieveAppMetadata()
    assertEquals("1970-01-01 01:00:00", metadata.date)
    assertEquals("machine",             metadata.machine)
    assertEquals("user",                metadata.user)
    assertEquals("test-version",        metadata.version)
  }

  @Test
  fun testSimpleSplit() {
    val emptyString          = ""
    val singleTokenString    = "aa"
    val twoTokensString      = "aa bb"
    val leadingSpacesString  = "  aa bb"
    val trailingSpacesString = "aa bb  "
    val nullTokensString     = "aa bb  cc dd"
    val spacesOrgy           = "  a   bb     ccc      dddd      "

    assertEquals(0, emptyString.simpleSplit().size)
    assertEquals(1, singleTokenString.simpleSplit().size)
    assertEquals(2, twoTokensString.simpleSplit().size)
    assertEquals(2, leadingSpacesString.simpleSplit().size)
    assertEquals(2, trailingSpacesString.simpleSplit().size)
    assertEquals(4, nullTokensString.simpleSplit().size)

    val spacesOrgyArray = spacesOrgy.simpleSplit()
    assertEquals(4,      spacesOrgyArray.size)
    assertEquals("a",    spacesOrgyArray[0])
    assertEquals("bb",   spacesOrgyArray[1])
    assertEquals("ccc",  spacesOrgyArray[2])
    assertEquals("dddd", spacesOrgyArray[3])
  }
}
