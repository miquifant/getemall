/**
 * URIs and Handlers related to uploading of files
 *
 * Created by miquifant on 2021-01-24
 */
package miquifant.getemall.api.controller

import miquifant.getemall.api.authentication.User
import miquifant.getemall.log.LoggerFactory
import miquifant.getemall.model.ExceptionalResponse
import miquifant.getemall.utils.Handler
import miquifant.getemall.utils.UploadReturnCode.*
import miquifant.getemall.utils.exception
import miquifant.getemall.utils.uploadedFile

import io.javalin.Javalin
import io.javalin.http.UploadedFile
import org.funktionale.either.Either
import org.funktionale.either.Either.Left
import org.funktionale.either.Either.Right
import org.funktionale.either.flatMap


object Uploader {
  object Uri {
    const val UPLOAD = "/api/upload"
  }
}

fun validateAvatar(file: UploadedFile): Either<UploadError, UploadedFile> {

  val tip = "Your profile picture should be a PNG, JPG, or GIF file under 1 MB in size"
  return when {

    file.size > 1048576 ->
      Left(TooLargeFileError("Too large image (${file.filename}). $tip"))

    listOf(".gif", ".jpg", ".jpeg", ".png").firstOrNull { file.extension.toLowerCase() == it } == null ->
      Left(WrongContentType("Invalid image type '${file.extension}'. $tip"))

    else -> Right(file)
  }
}


object UploaderController {

  private val logger = LoggerFactory.logger(UploaderController::class.java.canonicalName)

  val uploadAvatar: (Javalin) -> Handler = { app ->
    { ctx ->
      val currentUser = ctx.sessionAttribute<User?>("curUser")
      if (currentUser != null) {

        ctx.uploadedFile("avatar", app).right().flatMap { file ->

          validateAvatar(file).right().map { avatar ->

            /* TODO Complete this method
             * [content]: the file-content as an [InputStream]
             * [contentType]: the content-type passed by the client
             * [size]: the size of the file in bytes
             * [filename]: the file-name reported by the client
             * [extension]: the file-extension, extracted from the [filename]
             */
            val tmpResponse = mapOf (
                "content-type" to avatar.contentType,
                "size" to avatar.size,
                "filename" to avatar.filename,
                "extension" to avatar.extension
            )
            /*FileUtil.streamToFile(file.content, "upload/${file.filename}") // FIXME */
            logger.info { "${currentUser.name} uploaded avatar: $tmpResponse" }
            ctx.status(201).json(tmpResponse)
          }
        }
        // 415 Invalid type / 422 Unprocessable file / 500 Unknown error
        .left().map { uploadError ->
          logger.error { "${currentUser.name} failed to upload avatar due an error: ${uploadError.message}" }
          ctx.exception(ExceptionalResponse.fromUploadError(uploadError))
        }
      }
      // 401 Unauthorized
      else ctx.exception(ExceptionalResponse.unauthorized)
    }
  }
}
