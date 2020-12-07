/**
 * LoggerName-based discriminator for logback.
 *
 * Created by miquifant on 2020-11-15
 */
package miquifant.getemall.log

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.sift.Discriminator


class LoggerNameBasedDiscriminator: Discriminator<ILoggingEvent> {

  private var started: Boolean = false

  companion object {
    private const val KEY = "loggerName"
  }

  override fun getDiscriminatingValue(iLoggingEvent: ILoggingEvent): String {
    return iLoggingEvent.loggerName
  }

  override fun getKey(): String {
    return KEY
  }

  override fun start() {
    started = true
  }

  override fun stop() {
    started = false
  }

  override fun isStarted(): Boolean {
    return started
  }
}
