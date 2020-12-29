/**
 * Tests of model classes for API responses.
 *
 * Created by miquifant on 2020-12-27
 */
package miquifant.getemall.model

import miquifant.getemall.model.ExceptionalResponse.Companion.badRequest
import miquifant.getemall.model.ExceptionalResponse.Companion.inserted
import miquifant.getemall.model.ExceptionalResponse.Companion.notFound
import miquifant.getemall.model.ExceptionalResponse.Companion.ok
import miquifant.getemall.model.ExceptionalResponse.Companion.unauthorized
import miquifant.getemall.model.ExceptionalResponse.Companion.unknownError
import miquifant.getemall.persistence.SQLReturnCode

import kotlin.test.Test
import kotlin.test.assertEquals


class TestApiEntities {

  @Test
  fun testUnknownError() {
    assertEquals(500, unknownError.code)
    assertEquals("Unknown error", unknownError.message)
  }

  @Test
  fun testBadRequest() {
    assertEquals(400, badRequest.code)
    assertEquals("Bad request", badRequest.message)
  }

  @Test
  fun testUnauthorized() {
    assertEquals(401, unauthorized.code)
    assertEquals("Unauthorized", unauthorized.message)
  }

  @Test
  fun testNotFound() {
    assertEquals(404, notFound.code)
    assertEquals("Not found", notFound.message)
  }

  @Test
  fun testInserted() {
    assertEquals(201, inserted.code)
    assertEquals("Inserted", inserted.message)
  }

  @Test
  fun testOk() {
    assertEquals(200, ok.code)
    assertEquals("Ok", ok.message)
  }

  @Test
  fun fromSQLReturnCode() {

    val sqlSucceeded   = SQLReturnCode.Succeeded
    val sqlDeleted     = SQLReturnCode.Deleted
    val sqlUpdated     = SQLReturnCode.Updated
    val sqlUnaltered   = SQLReturnCode.Unaltered
    val sqlInserted    = SQLReturnCode.Inserted
    val sqlNotFound    = SQLReturnCode.NotFound

    val sqlUniqueError = SQLReturnCode.UniqueError("unique error")
    val sqlFkError     = SQLReturnCode.FKError("fk error")
    val sqlDbError     = SQLReturnCode.DBError("db error")

    assertEquals(ok,       ExceptionalResponse.fromSQLReturnCode(sqlSucceeded))
    assertEquals(ok,       ExceptionalResponse.fromSQLReturnCode(sqlDeleted))
    assertEquals(ok,       ExceptionalResponse.fromSQLReturnCode(sqlUpdated))
    assertEquals(ok,       ExceptionalResponse.fromSQLReturnCode(sqlUnaltered))
    assertEquals(inserted, ExceptionalResponse.fromSQLReturnCode(sqlInserted))
    assertEquals(notFound, ExceptionalResponse.fromSQLReturnCode(sqlNotFound))

    val resUniqueError = ExceptionalResponse.fromSQLReturnCode(sqlUniqueError)
    assertEquals(422, resUniqueError.code)
    assertEquals("unique error", resUniqueError.message)

    val resFkError = ExceptionalResponse.fromSQLReturnCode(sqlFkError)
    assertEquals(422, resFkError.code)
    assertEquals("fk error", resFkError.message)

    val resDbError = ExceptionalResponse.fromSQLReturnCode(sqlDbError)
    assertEquals(503, resDbError.code)
    assertEquals("db error", resDbError.message)
  }
}
