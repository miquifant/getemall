@file:JvmName("PersistenceCommons")
/**
 * Persistence module with Database management related objects and functions.
 *
 * Created by miquifant on 2020-11-14
 */
package miquifant.getemall.persistence

import miquifant.getemall.persistence.SQLReturnCode.Code.*
import miquifant.getemall.utils.toSingleLine


sealed class SQLReturnCode (private val code: Code) {

  internal enum class Code {
    SUCCEEDED,
    DELETED,
    INSERTED,
    UPDATED,
    UNALTERED,
    NOT_FOUND,
    ERROR,            // Unknown error
    DB_ERROR,         // Technical (like connection error)
    CONSTRAINT_ERROR, // IntegrityConstraintViolation (use only when not able specify the ICV)
    UNIQUE_ERROR,     // Unique index violation (like name already taken)
    FK_ERROR;         // Referential integrity constraint violation (like owner not found or organization is not empty)
  }
  object Succeeded: SQLReturnCode (SUCCEEDED)
  object Deleted:   SQLReturnCode (DELETED)
  object Inserted:  SQLReturnCode (INSERTED)
  object Updated:   SQLReturnCode (UPDATED)
  object Unaltered: SQLReturnCode (UNALTERED)
  object NotFound:  SQLReturnCode (NOT_FOUND)

  open class Error internal constructor (
      open val message: String,
      code: Code = ERROR): SQLReturnCode(code)

  data class DBError (override val message: String): Error(message, DB_ERROR)

  open class ConstraintError internal constructor (
      override val message: String,
      code: Code = CONSTRAINT_ERROR): Error(message, code)

  data class UniqueError (override val message: String): ConstraintError(message, UNIQUE_ERROR)
  data class FKError     (override val message: String): ConstraintError(message, FK_ERROR)

  data class Constraint internal constructor (val id: Regex, val code: SQLReturnCode.ConstraintError)

  companion object {

    fun error(message: String): Error = Error(message)

    fun constraintErrorFromMessage(message: String?, constraints: List<Constraint>): ConstraintError {
      val msg = message?.toSingleLine() ?: ""
      return constraints.mapNotNull { (regex, code) ->
        if (code.isError() && regex.matches(msg)) code
        else null
      }.firstOrNull() ?: ConstraintError("Integrity constraint violation", CONSTRAINT_ERROR)
    }
  }

  fun isError(): Boolean = code.toString().endsWith("ERROR")

  override fun toString(): String = code.toString()
}
