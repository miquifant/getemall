/**
 * Utilities and common functions.
 *
 * Created by miquifant on 2020-10-18
 */
package miquifant.getemall.utils

import java.text.SimpleDateFormat
import java.util.Date

// Date related functions
private val sdfISO8601 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

private fun dateFormatter(sdf: SimpleDateFormat) = { date: Date? -> sdf.format(date) }

val formatISO8601Date: (Date) -> String = dateFormatter(sdfISO8601)

/**
 * Application metadata from MANIFEST.MF
 */
data class AppMetadata(val version: String, val date: String, val user: String, val machine: String)
private object AppMetadataObject {
  // Needs to be an object to allow reflection
  // They'll be null (and will take defaults) only in UNpackaged version, that is during debug from IDE
  val title: String   = AppMetadata::class.java.`package`.implementationTitle   ?: "0 : user : machine"
  val version: String = AppMetadata::class.java.`package`.implementationVersion ?: "test-version"
}

fun retrieveAppMetadata(): AppMetadata {
  val data = Regex("(.*) : (.*) : (.*)").matchEntire(AppMetadataObject.title)!!.groupValues
  val date = formatISO8601Date(Date(data[1].toLong()))
  val user = data[2]
  val machine = data[3]
  return AppMetadata(AppMetadataObject.version, date, user, machine)
}

/**
 * Splits a String yielding a String Array with no empty elements
 * Example:    >····'····'····'····'····'····'····'·<
 * The String: " app  command  --option1   --option2 "
 * will return ["app", "command", "--option1", "--option2"]
 */
fun String.simpleSplit(): Array<String> =
    this.split(Regex(" ")).toList().mapNotNull { e -> if (e.isBlank()) null else e }.toTypedArray()
