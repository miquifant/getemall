/**
 * Tests of Database management related objects and functions.
 *
 * Created by miquifant on 2020-12-13
 */
package miquifant.getemall.persistence

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class TestPersistenceCommons {

  @Test
  fun testSQLReturnCodes() {
    val succeeded       = SQLReturnCode.Succeeded
    val deleted         = SQLReturnCode.Deleted
    val inserted        = SQLReturnCode.Inserted
    val updated         = SQLReturnCode.Updated
    val unaltered       = SQLReturnCode.Unaltered
    val notFound        = SQLReturnCode.NotFound

    assertFalse(succeeded.isError(), "SUCCEEDED is not an error return code")
    assertFalse(deleted.isError(),   "DELETED is not an error return code")
    assertFalse(inserted.isError(),  "INSERTED is not an error return code")
    assertFalse(updated.isError(),   "UPDATED is not an error return code")
    assertFalse(unaltered.isError(), "UNALTERED is not an error return code")
    assertFalse(notFound.isError(),  "NOT_FOUND is not an error return code")
  }

  @Test
  fun testErrorSQLReturnCodes() {
    val error           = SQLReturnCode.Error           ("Unknown error")
    val dbError         = SQLReturnCode.DBError         ("Technical error")
    val constraintError = SQLReturnCode.ConstraintError ("Integrity constraint violation error")
    val uniqueError     = SQLReturnCode.UniqueError     ("Name already taken")
    val fkError         = SQLReturnCode.FKError         ("Owner not found")

    assertTrue(error.isError(),           "ERROR is an error return code")
    assertTrue(dbError.isError(),         "DB_ERROR is an error return code")
    assertTrue(constraintError.isError(), "CONSTRAINT_ERROR is an error return code")
    assertTrue(uniqueError.isError(),     "UNIQUE_ERROR is an error return code")
    assertTrue(fkError.isError(),         "KF_ERROR is an error return code")

    assertEquals("Unknown error",                        error.message)
    assertEquals("Technical error",                      dbError.message)
    assertEquals("Integrity constraint violation error", constraintError.message)
    assertEquals("Name already taken",                   uniqueError.message)
    assertEquals("Owner not found",                      fkError.message)
  }

  @Test
  fun testConstraintErrorFromMessage() {
    val constraints = listOf (
        SQLReturnCode.Constraint(
            Regex("(?i).*table1_column1_UN.*"),
            SQLReturnCode.UniqueError("Existing value for column1")),
        SQLReturnCode.Constraint(
            Regex("(?i).*table1_table2_FK.*"),
            SQLReturnCode.FKError("Value not found in table2"))
    )

    val msgErrDupl = """
      |java.sql.SQLIntegrityConstraintViolationException:
      |(conn=XXX) Duplicate entry 'value1' for key 'table1_column1_UN'
      |""".trimMargin().trim()

    val msgErrFK = """
      |java.sql.SQLIntegrityConstraintViolationException:
      |(conn=XXX) Cannot add or update a child row:
      |a foreign key constraint fails fails (`db1`.`table1`, CONSTRAINT `table1_table2_FK` FOREIGN KEY (`column2`) REFERENCES `table2` (`id`) ON UPDATE CASCADE)
      |""".trimMargin().trim()

    val errConstraint = SQLReturnCode.constraintErrorFromMessage(null, constraints)
    // Assert it isn't a subclass
    assertFalse(errConstraint is SQLReturnCode.UniqueError, "Returned error should not be Unique Error")
    assertFalse(errConstraint is SQLReturnCode.FKError, "Returned error should not be FK Error")
    assertEquals("Integrity constraint violation", errConstraint.message)

    val errDuplicates = SQLReturnCode.constraintErrorFromMessage(msgErrDupl, constraints)
    assertTrue(errDuplicates is SQLReturnCode.UniqueError, "Returned error should be Unique Error")
    assertEquals("Existing value for column1", errDuplicates.message)

    val errForeignKey = SQLReturnCode.constraintErrorFromMessage(msgErrFK, constraints)
    assertTrue(errForeignKey is SQLReturnCode.FKError, "Returned error should be FK Error")
    assertEquals("Value not found in table2", errForeignKey.message)
  }
}
