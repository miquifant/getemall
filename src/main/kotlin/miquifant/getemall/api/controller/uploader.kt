/**
 * TODO
 *
 * Created by miquifant on 2021-01-24
 */
package miquifant.getemall.api.controller

import miquifant.getemall.log.LoggerFactory
import miquifant.getemall.model.ExceptionalResponse
import miquifant.getemall.utils.Handler
import miquifant.getemall.utils.exception
import miquifant.getemall.utils.uploadedFiles

import io.javalin.Javalin
import io.javalin.core.JettyUtil
import io.javalin.http.UploadedFile
import org.eclipse.jetty.server.handler.ContextHandler.MAX_FORM_CONTENT_SIZE_KEY


object Uploader {
  object Uri {
    const val UPLOAD = "/api/upload"
  }
}

object UploaderController {

  private val logger = LoggerFactory.logger(UploaderController::class.java.canonicalName)

  // FIXME naming
  val daledale: (Javalin) -> Handler = { app ->
    { ctx ->

      val jettyErrors = mutableListOf<String>()
      val jettyServer = JettyUtil.getOrDefault(app.config.inner.server)

      val uploadedFile: UploadedFile? = ctx.uploadedFiles (
          jettyErrors,
          "avatar",
          maxFileSize = jettyServer.getAttribute(MAX_FORM_CONTENT_SIZE_KEY) as? Long ?: -1L
      )
      if (uploadedFile != null) {
        /*
         * [content]: the file-content as an [InputStream]
         * [contentType]: the content-type passed by the client
         * [size]: the size of the file in bytes
         * [filename]: the file-name reported by the client
         * [extension]: the file-extension, extracted from the [filename]
         *
         * Tip: Your profile picture should be a PNG, JPG, or GIF file under 1 MB in size.
         * For the best quality rendering, we recommend keeping the image at about 500 by 500 pixels.
         */
        val tip = "Your profile picture should be a PNG, JPG, or GIF file under 1 MB in size"
        val err: String? = when {
          uploadedFile.size > 1048576 -> {
            "File is too big (${uploadedFile.size / 1024L / 1024L} MB). $tip"
          }
          listOf("gif", "jpg", "jpeg", "png").firstOrNull { uploadedFile.extension.toLowerCase() == it } == null -> {
            "Invalid file type. $tip"
          }
          else -> null
        }
        if (err == null) {
          ctx.json(mapOf(
              "content-type" to uploadedFile.contentType,
              "size" to uploadedFile.size,
              "filename" to uploadedFile.filename,
              "extension" to uploadedFile.extension
          ))
          /*FileUtil.streamToFile(uploadedFile.content, "upload/${uploadedFile.filename}")*/
        }
        // ??? ???????????
        else ctx.exception(ExceptionalResponse(400, err))
      }
      // 400 Bad request
      else {
        logger.info { jettyErrors.joinToString("\n") { it } } // TODO
        ctx.exception(ExceptionalResponse.badRequest)         // TODO TOO
      }
    }
  }
}
