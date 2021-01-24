@file:JvmName("ApiEntities")
/**
 * Data classes with entities returned in getemall API.
 *
 * Created by miquifant on 2020-12-25
 */
package miquifant.getemall.model

import miquifant.getemall.persistence.SQLReturnCode
import miquifant.getemall.persistence.SQLReturnCode.*
import miquifant.getemall.utils.UploadReturnCode.*


typealias Patch = Map<String, Any?>

data class ExceptionalResponse(val code: Int, val message: String) {

  companion object {

    val unknownError = ExceptionalResponse(500, "Unknown error")

    val badRequest = ExceptionalResponse(400, "Bad request")
    val unauthorized = ExceptionalResponse(401, "Unauthorized")
    val notFound = ExceptionalResponse(404, "Not found")
    val unsupportedMediaType = ExceptionalResponse(415, "Unsupported media type")

    val inserted = ExceptionalResponse(201, "Inserted")
    val ok = ExceptionalResponse(200, "Ok")

    fun fromSQLReturnCode(sqlReturnCode: SQLReturnCode): ExceptionalResponse {
      return when (sqlReturnCode) {

        // 503 Service unavailable: If a technical error occurred "Service unavailable"
        is DBError         -> ExceptionalResponse(503, sqlReturnCode.message)

        // 422 Unprocessable entity: If a functional error occurred "Name already taken"
        //     according to https://stackoverflow.com/questions/3825990/http-response-code-for-post-when-resource-already-exists/49709547#49709547
        is ConstraintError -> ExceptionalResponse(422, sqlReturnCode.message)

        is NotFound        -> notFound

        is Inserted        -> inserted

        // 200 Ok: Query Succeeded, Deleted, Updated, Unaltered (all with response body)
        else               -> ok        // Succeeded, Deleted, Updated, Unaltered
      }
    }

    fun fromUploadError(uploadError: UploadError): ExceptionalResponse {
      return when (uploadError) {

        // 415 Unsupported media type with custom message
        is WrongContentType     -> ExceptionalResponse(415, uploadError.message)

        // 422 Unprocessable entity: If file is wrong somehow
        is FileNotReceivedError -> ExceptionalResponse(422, uploadError.message)
        is TooLargeFileError    -> ExceptionalResponse(422, uploadError.message)

        // 500 Internal server error: Unexpected
        else                    -> unknownError
      }
    }
  }
}
