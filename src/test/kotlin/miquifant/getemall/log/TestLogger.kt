/**
 * Test javalin utils functions.
 *
 * Created by miquifant on 2020-12-10
 */
package miquifant.getemall.log

import kotlin.test.Test
import kotlin.test.assertNull


class TestLogger {

  @Test
  fun testDefaultLogger() {

    // Actually we just test that all logger methods work without Exception
    // but the logging functionallity itself is implemented in an external library
    // and it's behavior changes according to a configuration.
    // We are not testing external library or configurations.
    // We just add here this code as an example of usage
    try {

      val logger = LoggerFactory.logger(this::class.java.canonicalName)
      val e = RuntimeException("some exception")

      logger.trace { "trace" }
      logger.debug { "debug" }
      logger.info  { "info"  }
      logger.warn  { "warn"  }
      logger.error { "error" }

      logger.traceWithThrowable(e) { "trace with ${e.message}" }
      logger.debugWithThrowable(e) { "debug with ${e.message}" }
      logger.infoWithThrowable(e)  { "info  with ${e.message}" }
      logger.warnWithThrowable(e)  { "warn  with ${e.message}" }
      logger.errorWithThrowable(e) { "error with ${e.message}" }

      logger.log(Level.INFO) { "info" }

      LoggerFactory.disableLogging()
      logger.error { "you won't see this message" }

    } catch (e: Exception) {

      assertNull(e, "No exception should be thrown")
    }
  }
}
