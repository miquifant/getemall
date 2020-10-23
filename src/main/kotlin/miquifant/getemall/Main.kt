@file:JvmName("Main")
/**
 * Getemall-API entry point.
 *
 * Created by miquifant on 2020-10-18
 */
package miquifant.getemall

import miquifant.getemall.command.*
import miquifant.getemall.utils.retrieveAppMetadata

import org.docopt.Docopt


private val metadata = retrieveAppMetadata()

private val banner: Banner = arrayOf(
    "--------------------------------------------------------------------------",
    " Getemall-API version ${metadata.version} (${metadata.date})",
    " Compiled by ${metadata.user}@${metadata.machine}",
    "--------------------------------------------------------------------------"
)

// Usage document
private val doc = """
  |Getemall-API ${metadata.version}
  |
  | Usage:
  |   getemall-api serve
  |   getemall-api help | -h | --help
  |   getemall-api version | --version
  |
  | Options:
  |   --version                Show version.
  |   -h, --help               Show this help and exits.
  |""".trimMargin().trim()

fun parseOpts(args: Array<String>): Opts =
    Docopt(doc).withHelp(false).parse(args.toList())

fun main(args: Array<String>) {

  val opts: Opts = parseOpts(args)

  if (opts["serve"] as Boolean) {
    TODO("$opts")
    //startServer(opts)
  }
  else {
    val exitStatus = when {

      opts["version"]   as Boolean ||
      opts["--version"] as Boolean -> VersionCommand(banner)

      opts["help"]      as Boolean ||
      opts["--help"]    as Boolean -> HelpCommand(doc)

      // If doc is changed and published before implementing some command
      // a NotYetImplemented command will be executed, throwing an exception
      else -> NotYetImplementedCommand(args[0])

    }.execute()
    System.exit(exitStatus)
  }
}
