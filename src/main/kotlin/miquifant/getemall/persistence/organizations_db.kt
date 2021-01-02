@file:JvmName("PersistenceOrganizations")
/**
 * Persistence module with SQL statements and DB access functions for managing Organizations.
 *
 * Created by miquifant on 2020-11-14
 */
package miquifant.getemall.persistence

import miquifant.getemall.log.Loggable
import miquifant.getemall.model.Organization
import miquifant.getemall.persistence.SQLReturnCode.Constraint
import miquifant.getemall.utils.ConnectionManager

import java.sql.SQLIntegrityConstraintViolationException
import java.sql.Statement.RETURN_GENERATED_KEYS


private object SQLorganization {

  val constraints: List<Constraint> = listOf(
      Constraint(Regex("(?i).*organization_name_UN.*"),  SQLReturnCode.UniqueError("Name already taken")),
      Constraint(Regex("(?i).*organization_users_FK.*"), SQLReturnCode.FKError("Owner not found"))
  )

  val list = """
    |SELECT id, name, owner
    |FROM organizations
    |ORDER BY id
  """.trimMargin().trim()

  val show = """
    |SELECT id, name, owner
    |FROM organizations
    |WHERE id = ?
  """.trimMargin().trim()

  val showByName = """
    |SELECT id, name, owner
    |FROM organizations
    |WHERE name = ?
  """.trimMargin().trim()

  val delete = """
    |DELETE FROM organizations
    |WHERE id = ?
  """.trimMargin().trim()

  val insert = """
    |INSERT INTO organizations (
    |  name, owner
    |) VALUES (?, ?)
  """.trimMargin().trim()

  val update = """
    |UPDATE organizations SET
    |  name = ?,
    |  owner = ?
    |WHERE id = ?
  """.trimMargin().trim()
}

fun retrieveOrganizationsList(db: ConnectionManager, logger: Loggable.Logger):
    Pair<SQLReturnCode, List<Organization>>
{
  val ret = mutableListOf<Organization>()
  return try {
    val stmt = db().prepareStatement(SQLorganization.list)
    val rs = stmt.executeQuery()
    while (rs.next()) {
      ret.add(Organization(
          id = rs.getInt(1),
          name = rs.getString(2),
          owner = rs.getInt(3)
      ))
    }
    stmt.closeOnCompletion()
    Pair(SQLReturnCode.Succeeded, ret)
  }
  // Technical (like connection error)
  catch (e: Exception) {
    val message = "Unable to recover organizations list due an internal error"
    logger.errorWithThrowable(e) { "$message: ${e.message}" }
    Pair(SQLReturnCode.DBError(message), ret)
  }
}

fun retrieveOrganization(id: Int, db: ConnectionManager, logger: Loggable.Logger):
    Pair<SQLReturnCode, List<Organization>>
{
  val ret = mutableListOf<Organization>()
  return try {
    val stmt = db().prepareStatement(SQLorganization.show).apply {
      setInt(1, id)
    }
    val rs = stmt.executeQuery()
    if (rs.next())
      ret.add(Organization(
          id = id,
          name = rs.getString(2),
          owner = rs.getInt(3)
      ))
    stmt.closeOnCompletion()
    Pair(SQLReturnCode.Succeeded, ret)
  }
  // Technical (like connection error)
  catch (e: Exception) {
    val message = "Unable to recover organization id='$id' due an internal error"
    logger.errorWithThrowable(e) { "$message: ${e.message}" }
    Pair(SQLReturnCode.DBError(message), ret)
  }
}

fun retrieveOrganization(name: String, db: ConnectionManager, logger: Loggable.Logger):
    Pair<SQLReturnCode, List<Organization>>
{
  val ret = mutableListOf<Organization>()
  return try {
    val stmt = db().prepareStatement(SQLorganization.showByName).apply {
      setString(1, name)
    }
    val rs = stmt.executeQuery()
    if (rs.next())
      ret.add(Organization(
          id = rs.getInt(1),
          name = name,
          owner = rs.getInt(3)
      ))
    stmt.closeOnCompletion()
    Pair(SQLReturnCode.Succeeded, ret)
  }
  // Technical (like connection error)
  catch (e: Exception) {
    val message = "Unable to recover organization '$name' due an internal error"
    logger.errorWithThrowable(e) { "$message: ${e.message}" }
    Pair(SQLReturnCode.DBError(message), ret)
  }
}

/**
 * Deletes requested organization, returning old instance if existed
 */
fun deleteOrganization(id: Int, db: ConnectionManager, logger: Loggable.Logger):
    Pair<SQLReturnCode, Organization?>
{
  val (queryResult, oldOrg) = retrieveOrganization(id, db, logger)
  val querySucceeded = queryResult == SQLReturnCode.Succeeded
  val orgExisted = querySucceeded && oldOrg.isNotEmpty()
  val retOrg: Organization? = if (orgExisted) oldOrg[0] else null
  return when {
    querySucceeded && orgExisted -> {
      val deleteRC = try {
        val stmt = db().prepareStatement(SQLorganization.delete).apply {
          setInt(1, id)
        }
        val rows = stmt.executeUpdate()
        stmt.closeOnCompletion()
        if (rows > 0)

          SQLReturnCode.Deleted

        else {
          val message = "Unknown problem while deleting organization id='$id'"
          logger.error { message }
          SQLReturnCode.error(message)
        }
      }
      // Functional (like object has child objects and cannot be deleted)
      // Since current data model doesn't define any child table for organizations, this exception won't be thrown.
      // However, they will be defined and we consider it better to implement the catch so
      // no modification will be required in this module if we define another table with FK refering this one.
      catch (e: SQLIntegrityConstraintViolationException) {
        val constraintError = SQLReturnCode.constraintErrorFromMessage(e.message, SQLorganization.constraints)
        logger.error {
          "Unable to delete organization '${retOrg!!.name}' due an error: ${constraintError.message}"
        }
        constraintError
      }
      // Technical (like connection error)
      catch (e: Exception) {
        val message = "Unable to delete organization id='$id' due an internal error"
        logger.errorWithThrowable(e) { "$message: ${e.message}" }
        SQLReturnCode.DBError(message)
      }
      Pair(deleteRC, retOrg)
    }
    querySucceeded -> {
      Pair(SQLReturnCode.Unaltered, retOrg)
    }
    // Technical (like connection error) - Unable to access it before deleting
    else -> {
      val message = "Unable to access organization id='$id' for deleting it"
      Pair(SQLReturnCode.DBError(message), retOrg)
    }
  }
}

/**
 * Receives an organization objet with some arbitrary id (recommended zero),
 * inserts it into the database and returns the object with the assigned id
 */
fun insertOrganization(organization: Organization, db: ConnectionManager, logger: Loggable.Logger):
    Pair<SQLReturnCode, Organization?> =
    try {
      val stmt = db().prepareStatement(SQLorganization.insert, RETURN_GENERATED_KEYS).apply {
        setString(1, organization.name)
        setInt(2, organization.owner)
      }
      val rows = stmt.executeUpdate()
      val id = stmt.generatedKeys.apply { next() }.getInt(1)
      stmt.closeOnCompletion()
      if (rows > 0)

        Pair(SQLReturnCode.Inserted, organization.copy(id = id))

      else {
        val message = "Unknown problem while persisting organization '${organization.name}'"
        logger.error { message }
        Pair(SQLReturnCode.error(message), null)
      }
    }
    // Functional (like name already taken or owner not found)
    catch (e: SQLIntegrityConstraintViolationException) {
      val constraintError = SQLReturnCode.constraintErrorFromMessage(e.message, SQLorganization.constraints)
      logger.error {
        "Unable to persist organization '${organization.name}' due an error: ${constraintError.message}"
      }
      Pair(constraintError, null)
    }
    // Technical (like connection error)
    catch (e: Exception) {
      val message = "Unable to persist organization '${organization.name}' due an internal error"
      logger.errorWithThrowable(e) { "$message: ${e.message}" }
      Pair(SQLReturnCode.DBError(message), null)
    }

/**
 * Receives an organization object with an id and some attributes,
 * and updates the record with the same id if it exists (if not, it will return `NOT_FOUND`)
 */
fun updateOrganization(organization: Organization, db: ConnectionManager, logger: Loggable.Logger):
    SQLReturnCode =
    try {
      val stmt = db().prepareStatement(SQLorganization.update).apply {
        setString(1, organization.name)
        setInt(2, organization.owner)
        setInt(3, organization.id)
      }
      val rows = stmt.executeUpdate()
      stmt.closeOnCompletion()
      if (rows == 1)

        SQLReturnCode.Updated

      else {
        logger.info { "Couldn't find organization to update" }
        SQLReturnCode.NotFound
      }
    }
    // Functional (like name already taken or owner not found)
    catch (e: SQLIntegrityConstraintViolationException) {
      val constraintError = SQLReturnCode.constraintErrorFromMessage(e.message, SQLorganization.constraints)
      logger.error {
        "Unable to update organization id='${organization.id}' due an error: ${constraintError.message}"
      }
      constraintError
    }
    // Technical (like connection error)
    catch (e: Exception) {
      val message = "Unable to update organization id='${organization.id}' due an internal error"
      logger.errorWithThrowable(e) { "$message: ${e.message}" }
      SQLReturnCode.DBError(message)
    }
