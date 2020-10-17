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
}
