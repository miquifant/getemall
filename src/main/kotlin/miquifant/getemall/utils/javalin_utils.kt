/**
 * Utilities and common functions for working with Javalin.
 *
 * Created by miquifant on 2020-10-25
 */
package miquifant.getemall.utils

import miquifant.getemall.model.ExceptionalResponse
import miquifant.getemall.model.ExceptionalResponse.Companion.unknownError
import miquifant.getemall.utils.AppRole.*

import io.javalin.core.security.Role
import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.http.UploadedFile
import miquifant.getemall.log.Loggable
import miquifant.getemall.log.Loggable.Logger
import org.eclipse.jetty.server.Request.MULTIPART_CONFIG_ELEMENT
import javax.servlet.MultipartConfigElement


typealias Handler = (Context) -> Unit
typealias ExceptionHandler = (Exception, Context) -> Unit
typealias AccessManager = (Handler, Context, Set<Role>) -> Unit
typealias RequestLogger = (Context, Float) -> Unit
typealias JavalinState = (Context) -> Any

enum class AppRole: Role {
  ANONYMOUS, REGULAR_USER, ADMIN
}

internal object GrantedFor {
  val admins        = setOf(ADMIN)
  val loggedInUsers = admins + setOf(REGULAR_USER)
  val anyone        = loggedInUsers + setOf(ANONYMOUS)
}

fun Context.exception(code: Int, message: String): Context {
  return this.exception(ExceptionalResponse(code, message))
}

fun Context.exception(res: ExceptionalResponse = unknownError): Context {
  return this.status(res.code).json(res)
}

class TooLargeFileException(partName: String? = null, maxFileSize: Long? = null): RuntimeException (
    "File${
      if (partName != null) " $partName" else ""
    } exceeds max filesize${
      if (maxFileSize != null) " $maxFileSize" else ""
    }"
)
/**
 * Gets first [UploadedFile] for the specified name, or null.
 *
 * TODO document this
 * He tenido que hacer esta función de extensión porque la función original Context.uploadedFiles
 * recibe únicamente el nombre del part e invoca a MultipartUtil.getUploadedFiles que crea un
 * MultipartConfigElement con maxFileSize = -1L, lo que supone una vulnerabilidad ante un ataque
 * de denegación de servicio si un usuario envía un fichero enorme.
 */
fun Context.uploadedFiles(feedback: MutableList<String>,
                          fileName: String,
                          location: String = System.getProperty("java.io.tmpdir"),
                          maxFileSize: Long = -1L,
                          maxRequestSize: Long = -1L,
                          fileSizeThreshold: Int = 0): UploadedFile? =

    if (isMultipartFormData()) {

      req.setAttribute(MULTIPART_CONFIG_ELEMENT,
          MultipartConfigElement(location, maxFileSize, maxRequestSize, fileSizeThreshold))

      try { req.parts } catch (e: Exception) {

        when {
          e is java.lang.IllegalStateException && e.message == "Multipart Mime part $fileName exceeds max filesize" ->
            feedback.add("File exceeds max filesize")
          e is org.eclipse.jetty.io.EofException ->
            feedback.add("File not received")
          else ->
            feedback.add("Unknown error: ${e.message}")
        }
        null

      }?.filter { it.submittedFileName != null && it.name == fileName }?.map { filePart ->

        UploadedFile(
            content = filePart.inputStream,
            contentType = filePart.contentType,
            contentLength = filePart.size.toInt(),
            filename = filePart.submittedFileName,
            extension = filePart.submittedFileName.replaceBeforeLast(".", ""),
            size = filePart.size)

      }?.firstOrNull()
    }
    else {
      feedback.add("wrong content type") // TODO
      null
    }
