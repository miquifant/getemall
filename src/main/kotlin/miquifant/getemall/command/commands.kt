/**
 * Functions for configuring app's commands.
 *
 * Created by miquifant on 2020-10-18
 */
package miquifant.getemall.command

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValue
import com.typesafe.config.ConfigValueFactory

import java.io.File


typealias Opts = Map<String, Any>
typealias Banner = Array<String>
typealias ExitStatus = Int

// Exit status
const val OS_OK    = 0
@Suppress("unused")
const val OS_ERROR = 1

/**
 * Environment variables accessed from code
 */
object EnvVars {
  const val APP_CONF = "GETEMALL_API_CONF"
  const val APP_CURR = "GETEMALL_API_CURR"
  const val PWD      = "PWD"
}

private fun appCurrentDirConfigValue(env: Config): ConfigValue =
    when {
      env.hasPath(EnvVars.APP_CURR) ->
        ConfigValueFactory.fromAnyRef(env.getString(EnvVars.APP_CURR))
      env.hasPath(EnvVars.PWD) ->
        ConfigValueFactory.fromAnyRef(env.getString(EnvVars.PWD))
      else ->
        ConfigValueFactory.fromAnyRef("/")
    }

private fun loadAppConfig(env: Config): Config =
    if(env.hasPath(EnvVars.APP_CONF)) {
      val appConf   = env.getString(EnvVars.APP_CONF)
      val confFile  = "getemall-api.conf"
      val appConfig = ConfigFactory.parseFile(File("$appConf/$confFile"))
      ConfigFactory.load(appConfig)
    } else {
      env // Skips mkat own config if APP_CONF not defined as environment variable
    }

private fun loadCustomConfig(customConfigFile: String): Config =
    ConfigFactory.parseFile(File(customConfigFile))

fun loadFullConfig(customConfigFile: String): Config {
  val environmentConfig = ConfigFactory.systemEnvironment()
  val systemConfig      = ConfigFactory.systemProperties()
  val appConfig         = loadAppConfig(environmentConfig)
  val customConfig      = loadCustomConfig(customConfigFile)

  return ConfigFactory.load(customConfig)
      .withFallback(appConfig)
      .withFallback(systemConfig)
      .withFallback(environmentConfig)
      .withValue("curdir", appCurrentDirConfigValue(environmentConfig))
}

abstract class Command (val name: String) {
  /**
   * Executes command with received options and returns an exitStatus
   */
  abstract fun execute(): ExitStatus
}

/**
 * If doc is changed and published before implementing some command
 * a NotYetImplemented command will be executed, throwing an exception
 */
class NotYetImplementedCommand (command: String): Command(command) {
  /**
   * @exception kotlin.NotImplementedError
   */
  override fun execute(): ExitStatus {
    TODO("Getemall-API command not yet implemented: '$name'")
  }
}

class HelpCommand (private val doc: String): Command("help") {
  override fun execute(): ExitStatus {
    println(doc)
    return OS_OK
  }
}

class VersionCommand (private val banner: Banner): Command("version") {
  override fun execute(): ExitStatus {
    banner.forEach { println(it) }
    return OS_OK
  }
}
