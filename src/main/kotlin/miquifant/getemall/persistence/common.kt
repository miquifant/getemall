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
    PATCHED,
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
  object Patched:   SQLReturnCode (PATCHED)
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

  /**
   * We expect some pattern in the message of the Exceptions produced by the violation of defined constraints.
   * Only tested against H2 (1.4.200) and MariaDB (2.7.0).
   *
   * H2:
   *   Unique index or primary key violation: "getemall.organization_name_un_INDEX_8 ON getemall.organizations(name) VALUES 3"; SQL statement: <...> [23505-200]
   *   Referential integrity constraint violation: "organization_users_fk: getemall.organizations FOREIGN KEY(owner) REFERENCES getemall.users(id) (0)"; SQL statement: <...> [23506-200]
   *   Referential integrity constraint violation: "<child>_organizations_fk: getemall.<child> FOREIGN KEY(<field>) REFERENCES getemall.organizations(id) (4)"; SQL statement: <...> [23503-200]
   *
   * MariaDB:
   *   Duplicate entry 'new org' for key 'organization_name_UN'
   *   Cannot add or update a child row: a foreign key constraint fails (`getemall`.`organizations`, CONSTRAINT `organization_users_FK` FOREIGN KEY (`owner`) REFERENCES `users` (`id`) ON UPDATE CASCADE)
   *   Cannot delete or update a parent row: a foreign key constraint fails (`getemall`.`<child>`, CONSTRAINT `<child>_organizations_FK` FOREIGN KEY (`<field>`) REFERENCES `organizations` (`id`) ON DELETE NO ACTION ON UPDATE CASCADE)')
   *
   * Examples:
   *   Constraint(Regex("(?i).*user_email_UN.*"), SQLReturnCode.UniqueError("Email already exists")),
   *   Constraint(Regex("(?i).*user_superpowers_FK.*"), SQLReturnCode.FKError("Invalid role")),
   *   Constraint(Regex("(?i).*organization_users_FK*"), SQLReturnCode.FKError("User is owner of some organization"))
   */
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
