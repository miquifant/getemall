@file:JvmName("Main")
/**
 * Getemall-API entry point.
 *
 * Created by miquifant on 2020-10-18
 */
package miquifant.getemall

import miquifant.getemall.utils.retrieveAppMetadata


private val metadata = retrieveAppMetadata()

fun main(args: Array<String>) {
  println("Hello world! ${metadata.user}, ${metadata.date}, ${metadata.machine}, ${metadata.version}")
}
