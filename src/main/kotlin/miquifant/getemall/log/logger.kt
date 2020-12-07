@file:JvmName("GetemallLogger")
/**
 * Logger module with the definition of a LoggerFactory.
 *
 * Created by miquifant on 2020-11-15
 */
package miquifant.getemall.log

import ch.qos.logback.classic.Logger
import org.slf4j.Logger as slfLogger
import org.slf4j.LoggerFactory as slfLoggerFactory


enum class Level {
  TRACE, DEBUG, INFO, WARN, ERROR
}

@Suppress("unused")
interface Loggable {

  val logger: Logger
  fun logger(name: String): Logger = LoggerFactory.logger(name)

  interface Logger {

    fun trace (msg: () -> String)
    fun debug (msg: () -> String)
    fun info  (msg: () -> String)
    fun warn  (msg: () -> String)
    fun error (msg: () -> String)

    fun traceWithThrowable (t: Throwable, msg: () -> String)
    fun debugWithThrowable (t: Throwable, msg: () -> String)
    fun infoWithThrowable  (t: Throwable, msg: () -> String)
    fun warnWithThrowable  (t: Throwable, msg: () -> String)
    fun errorWithThrowable (t: Throwable, msg: () -> String)

    fun log (level: Level, msg: () -> String)
  }
}

@Suppress("unused")
object LoggerFactory {

  fun logger(name: String) = DefaultLogger(slfLoggerFactory.getLogger(name))

  fun disableLogging() {
    // This allows us to access the underlying root logger. That's why we use
    // fully quialified names to access to the method.
    with(org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger) {
      level = ch.qos.logback.classic.Level.OFF
    }
  }
}

class DefaultLogger(private val logger: slfLogger): Loggable.Logger {

  override fun trace(msg: () -> String) {
    if (logger.isTraceEnabled) logger.trace(msg.invoke())
  }
  override fun debug(msg: () -> String) {
    if (logger.isDebugEnabled) logger.debug(msg.invoke())
  }
  override fun info(msg: () -> String) {
    if (logger.isInfoEnabled) logger.info(msg.invoke())
  }
  override fun warn(msg: () -> String) {
    if (logger.isWarnEnabled) logger.warn(msg.invoke())
  }
  override fun error(msg: () -> String) {
    if (logger.isErrorEnabled) logger.error(msg.invoke())
  }

  override fun traceWithThrowable(t: Throwable, msg: () -> String) {
    if (logger.isTraceEnabled) logger.trace(msg.invoke(), t)
  }
  override fun debugWithThrowable(t: Throwable, msg: () -> String) {
    if (logger.isDebugEnabled) logger.debug(msg.invoke(), t)
  }
  override fun infoWithThrowable(t: Throwable, msg: () -> String) {
    if (logger.isInfoEnabled) logger.info(msg.invoke(), t)
  }
  override fun warnWithThrowable(t: Throwable, msg: () -> String) {
    if (logger.isWarnEnabled) logger.warn(msg.invoke(), t)
  }
  override fun errorWithThrowable(t: Throwable, msg: () -> String) {
    if (logger.isErrorEnabled) logger.error(msg.invoke(), t)
  }

  private val logmethods = mapOf (
      Level.TRACE to DefaultLogger::trace,
      Level.DEBUG to DefaultLogger::debug,
      Level.INFO  to DefaultLogger::info,
      Level.WARN  to DefaultLogger::warn,
      Level.ERROR to DefaultLogger::error
  )

  override fun log(level: Level, msg: () -> String) {
    logmethods[level]?.invoke(this, msg)
  }
}
