/**
 * Functions for configuring app's commands.
 *
 * Created by miquifant on 2020-10-18
 */
package miquifant.getemall.command


typealias Opts = Map<String, Any>
typealias Banner = Array<String>
typealias ExitStatus = Int

// Exit status
const val OS_OK    = 0
@Suppress("unused")
const val OS_ERROR = 1

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
