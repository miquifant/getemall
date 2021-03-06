/**
 * Test general commands functions.
 *
 * Created by miquifant on 2020-10-18
 */
package miquifant.getemall.command

import java.io.File

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail


class TestCommands {

  @Test
  fun testLoadFullConfig() {
    val testConfFile = "getemall/command/test-load-custom-config.conf"

    val config = loadFullConfig(File(this.javaClass.classLoader.getResource(testConfFile).file).canonicalFile.path)

    val apiPort = "api_port"
    assertTrue(config.hasPath(apiPort), "config should define 'api_port' attribute")
    assertEquals("6789", config.getString(apiPort))
  }

  @Test
  fun testNotYetImplementedCommand() {
    val cmd = NotYetImplementedCommand("command")
    assertEquals("command", cmd.name)
    try {
      cmd.execute()
      fail("Not implemented commands should throw an exception")
    } catch (e: NotImplementedError) {
      assertEquals("An operation is not implemented: Getemall command not yet implemented: 'command'", e.message)
    }
  }

  @Test
  fun testHelpCommand() {
    val cmd = HelpCommand("document")
    assertEquals("help", cmd.name)
    assertEquals(OS_OK, cmd.execute())
  }

  @Test
  fun testVersionCommand() {
    val cmd = VersionCommand(arrayOf("line1", "line2"))
    assertEquals("version", cmd.name)
    assertEquals(OS_OK, cmd.execute())
  }
}
