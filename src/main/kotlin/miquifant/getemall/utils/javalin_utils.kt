/**
 * Utilities and common functions for working with Javalin.
 *
 * Created by miquifant on 2020-10-25
 */
package miquifant.getemall.utils

import io.javalin.Javalin
import io.javalin.core.JettyUtil
import miquifant.getemall.model.ExceptionalResponse
import miquifant.getemall.model.ExceptionalResponse.Companion.unknownError
import miquifant.getemall.utils.AppRole.*
import miquifant.getemall.utils.UploadReturnCode.*
import miquifant.getemall.utils.UploadReturnCode.Code.*

import io.javalin.core.security.Role
import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.http.UploadedFile
import org.eclipse.jetty.io.EofException
import org.eclipse.jetty.server.Request.MULTIPART_CONFIG_ELEMENT
import org.eclipse.jetty.server.handler.ContextHandler.MAX_FORM_CONTENT_SIZE_KEY
import org.funktionale.either.Either
import org.funktionale.either.Either.Companion.left
import org.funktionale.either.Either.Companion.right
import org.funktionale.either.Either.Left
import org.funktionale.either.Either.Right
import org.funktionale.either.flatMap

import java.io.IOException
import javax.servlet.MultipartConfigElement
import javax.servlet.ServletException
import javax.servlet.http.Part


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

sealed class UploadReturnCode (private val code: Code) {

  internal enum class Code {
    FILE_NOT_RECEIVED,
    TOO_LARGE_FILE,
    WRONG_CONTENT_TYPE,
    ERROR
  }

  open class UploadError internal constructor (
      open val message: String,
      code: Code = Code.ERROR): UploadReturnCode(code)

  data class FileNotReceivedError (
      override val message: String = "File not received"): UploadError(message, FILE_NOT_RECEIVED)

  data class TooLargeFileError (
      override val message: String = "File is too large"): UploadError(message, TOO_LARGE_FILE)

  data class WrongContentType (
      override val message: String = "Unsupported media type"): UploadError(message, WRONG_CONTENT_TYPE)

  companion object {
    fun error(message: String): UploadError = UploadError(message)
  }
}

fun Context.exception(code: Int, message: String): Context {
  return this.exception(ExceptionalResponse(code, message))
}

fun Context.exception(res: ExceptionalResponse = unknownError): Context {
  return this.status(res.code).json(res)
}

/**
 * This method calls to `Request.getParts()` and, therefore, it starts reading the stream of the uploaded files.
 * So this is the place to catch the IllegalStateException, when uploaded file is larger than defined max filesize.
 */
fun Context.extractPartsFromRequest(): Either<UploadError, Collection<Part>> =
    try {
      Right(req.parts)
    } catch (e: IllegalStateException) {
      Left(TooLargeFileError("File exceeds max filesize"))
    } catch (e: EofException) {
      Left(FileNotReceivedError("File not received"))
    } catch (e: ServletException) {
      Left(WrongContentType("Unsupported content type ('multipart/form-data' expected)"))
    } catch (e: IOException) {
      Left(UploadReturnCode.error(e.message ?: "Unexpected transfer error"))
    }

/**
 * Gets first [UploadedFile] for the specified name, or null.
 *
 * We have created this extension function because the original [Context.uploadedFile] receives just the part name,
 *   and invokes `MultipartUtil.getUploadedFiles`, which creates a [MultipartConfigElement] with maxFileSize = -1L
 *   (that means "no limit", so a user could send us a file of 1 GB or even more).
 * That behaviour could lead us to a security hole,
 *   even if we validate later the [UploadedFile] is smaller than a certain size,
 *   we wouldn't have prevented the file from being transferred to the server,
 *   with a lot of resources usage.
 * Any endpoint that lets you tie up a massive amount of resources
 *   is vulnerable to denial-of-service attacks (intentional or not).
 *
 * This function allows developer to specify any parameter of the [MultipartConfigElement] to help mitigating risks.
 *
 * Additionally, we changed the returned type to return either the [UploadedFile] or a [UploadError].
 */
fun Context.uploadedFile(fileName: String,
                         location: String = System.getProperty("java.io.tmpdir"),
                         maxFileSize: Long = -1L,
                         maxRequestSize: Long = -1L,
                         fileSizeThreshold: Int = 0): Either<UploadError, UploadedFile> {

    req.setAttribute(MULTIPART_CONFIG_ELEMENT,
        MultipartConfigElement(location, maxFileSize, maxRequestSize, fileSizeThreshold))

    // on Left -> Too large file; File not received; Wrong content type; I/O error
    return extractPartsFromRequest()

        .right().flatMap { parts ->

          val uploadedFile = parts.filter { it.submittedFileName != null && it.name == fileName }
              .map { filePart ->
                UploadedFile(
                    content = filePart.inputStream,
                    contentType = filePart.contentType,
                    contentLength = filePart.size.toInt(),
                    filename = filePart.submittedFileName,
                    extension = filePart.submittedFileName.replaceBeforeLast(".", ""),
                    size = filePart.size)
              }.firstOrNull()

          // Ok (Uploaded)
          if (uploadedFile != null) right(uploadedFile)

          // Unprocessable request, File not received
          else left(FileNotReceivedError("File not received for part '$fileName'"))
        }
}

/**
 * Gets first [UploadedFile] for the specified name, or null.
 *
 * If an uploaded file exceeds the max filesize defined in jetty's [MAX_FORM_CONTENT_SIZE_KEY],
 *   the function will return [TooLargeFileError].
 *
 * This is a convenience method for adopting the behavior we believe will be more commonly required,
 *   which consists of not specifying the max filesize in each call,
 *   but using the one defined in the underlying jetty config.
 */
fun Context.uploadedFile(fileName: String, app: Javalin): Either<UploadError, UploadedFile> {
  val jetty = try { JettyUtil.getOrDefault(app.config.inner.server) } catch (e: Exception) { null }
  val maxFileSize = jetty?.getAttribute(MAX_FORM_CONTENT_SIZE_KEY) as? Long ?: -1L
  return this.uploadedFile(fileName, maxFileSize = maxFileSize)
}
