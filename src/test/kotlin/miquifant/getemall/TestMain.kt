/**
 * Tests for Main class.
 *
 * Created by miquifant on 2020-10-18
 */
package miquifant.getemall

import miquifant.getemall.utils.simpleSplit

import org.junit.Rule
import org.junit.contrib.java.lang.system.ExpectedSystemExit

import kotlin.test.*


class TestMain {

  @get:Rule
  val exit: ExpectedSystemExit = ExpectedSystemExit.none()

  @Test
  fun testParseOptsUnexistingCommand() {
    val args = "unexisting"
    exit.expectSystemExitWithStatus(1)
    parseOpts(args.simpleSplit())
  }

  @Test
  fun testParseOptsNoCommandNoOpts() {
    val args = ""
    exit.expectSystemExitWithStatus(1)
    parseOpts(args.simpleSplit())
  }

  @Test
  fun testParseOptsNoCommandWrongOpts() {
    val args = "--unexisting-option"
    exit.expectSystemExitWithStatus(1)
    parseOpts(args.simpleSplit())
  }

  @Test
  fun testParseOptsNoCommandOk() {
    val argsHelp1 = "-h"
    val optsHelp1 = parseOpts(argsHelp1.simpleSplit())
    assertTrue(optsHelp1["--help"] as Boolean)

    val argsHelp2 = "--help"
    val optsHelp2 = parseOpts(argsHelp2.simpleSplit())
    assertTrue(optsHelp2["--help"] as Boolean)

    val argsVersion = "--version"
    val optsVersion = parseOpts(argsVersion.simpleSplit())
    assertTrue(optsVersion["--version"] as Boolean)
  }

  @Test
  fun testParseOptsHelpWrongOpts() {
    val args = "help --unexisting-option"
    exit.expectSystemExitWithStatus(1)
    parseOpts(args.simpleSplit())
  }

  @Test
  fun testParseOptsHelpOk() {
    val args = "help"
    val opts = parseOpts(args.simpleSplit())
    assertTrue(opts["help"] as Boolean)
  }

  @Test
  fun testParseOptsVersionWrongOpts() {
    val args = "version --unexisting-option"
    exit.expectSystemExitWithStatus(1)
    parseOpts(args.simpleSplit())
  }

  @Test
  fun testParseOptsVersionOk() {
    val args = "version"
    val opts = parseOpts(args.simpleSplit())
    assertTrue(opts["version"] as Boolean)
  }

  @Test
  fun testParseOptsServeWrongOpts() {
    val args = "serve --unexisting-option"
    exit.expectSystemExitWithStatus(1)
    parseOpts(args.simpleSplit())
  }

  @Test
  fun testParseOptsServeOk() {
    val args = "serve"
    val opts = parseOpts(args.simpleSplit())
    assertTrue(opts["serve"] as Boolean)
  }

  @Test
  fun testMainHelp() {
    val args = "help"
    exit.expectSystemExitWithStatus(0)
    main(args.simpleSplit())
  }

  @Test
  fun testMainVersion() {
    val args = "version"
    exit.expectSystemExitWithStatus(0)
    main(args.simpleSplit())
  }

  @Test
  fun testMainServe() {
    val args = "serve"
    try {
      main(args.simpleSplit())
      fail("serve command is not implemented and should throw an exception")
    } catch (e: NotImplementedError) {

      assertNotNull("Not Implemented Error message should not be null", e.message)

      val beginOdMessage = "An operation is not implemented: "
      assertTrue (e.message!!.startsWith(beginOdMessage),
          "Not Implemented Error message should start with '$beginOdMessage'")

      val messageContent = " serve=true"
      assertTrue (
          e.message!!.contains(messageContent),
          "Not Implemented Error message should contain '$messageContent'"
      )
    } catch (e: Exception) {
      fail("serve command is not implemented and should throw NotImplementedError")
    }
  }
}
