/**
 * Test persistence functions for Organizations.
 *
 * Created by miquifant on 2020-11-14
 */
package miquifant.getemall.persistence

import miquifant.getemall.log.Loggable.Logger
import miquifant.getemall.log.LoggerFactory
import miquifant.getemall.model.Organization
import miquifant.getemall.testingutils.initDatabaseConnection
import miquifant.getemall.utils.ConnectionManager

import java.util.*

import kotlin.test.*


class TestOrganizations {

  private lateinit var logger: Logger
  private lateinit var db: ConnectionManager
  private val tmpDatabase by lazy { "db_${Date().time}" }

  @BeforeTest
  fun setup() {
    logger = LoggerFactory.logger(this::class.java.canonicalName)
    db = initDatabaseConnection(tmpDatabase)
  }

  @AfterTest
  fun disposeDatabaseConnection() {
    db().close()
  }

  @Test
  fun testRetrieveOrganizationsList() {
    assertNotNull(db(), "Unable to execute test due an error with database connection")

    val (ret, organizations) = retrieveOrganizationsList(db, logger)
    assertEquals(SQLReturnCode.Succeeded, ret)
    assertEquals(2, organizations.size)

    val connection = db()
    connection.createStatement().execute("TRUNCATE TABLE organizations")
    val (retEmpty, noOrganizations) = retrieveOrganizationsList(db, logger)
    assertEquals(SQLReturnCode.Succeeded, retEmpty)
    assertEquals(0, noOrganizations.size)

    // -----------
    // TEST ERRORS
    // -----------

    // Will fail due technical error (like connection error)
    connection.close()

    val (retKO, emptyList) = retrieveOrganizationsList(db, logger)
    assertTrue(retKO is SQLReturnCode.DBError, "Query should fail")
    assertEquals("Unable to recover organizations list due an internal error", retKO.message)
    assertTrue(emptyList.isEmpty(), "Query should return no results")
  }

  @Test
  fun testRetrieveOrganizationById() {
    assertNotNull(db(), "Unable to execute test due an error with database connection")

    val existingOrg = 1
    val unexistingOrg = 0

    val (ret1, orgsWithExistingId) = retrieveOrganization(existingOrg, db, logger)
    assertEquals(SQLReturnCode.Succeeded, ret1)
    assertEquals(1, orgsWithExistingId.size)

    val (ret2, orgsWithUnexistingId) = retrieveOrganization(unexistingOrg, db, logger)
    assertEquals(SQLReturnCode.Succeeded, ret2)
    assertEquals(0, orgsWithUnexistingId.size)

    // -----------
    // TEST ERRORS
    // -----------

    // Will fail due technical error (like connection error)
    db().close()

    val (retKO, emptyList) = retrieveOrganization(existingOrg, db, logger)
    assertTrue(retKO is SQLReturnCode.DBError, "Return should be DBError")
    assertEquals("Unable to recover organization id='$existingOrg' due an internal error", retKO.message)
    assertTrue(emptyList.isEmpty(), "Query should return no results")
  }

  @Test
  fun testRetrieveOrganizationByName() {
    assertNotNull(db(), "Unable to execute test due an error with database connection")

    val existingOrgName = "Fake Company"
    val unexistingOrgName = "unexisting"

    val (ret1, orgsWithExistingName) = retrieveOrganization(existingOrgName, db, logger)
    assertEquals(SQLReturnCode.Succeeded, ret1)
    assertEquals(1, orgsWithExistingName.size)

    val (ret2, orgsWithUnexistingName) = retrieveOrganization(unexistingOrgName, db, logger)
    assertEquals(SQLReturnCode.Succeeded, ret2)
    assertEquals(0, orgsWithUnexistingName.size)

    // -----------
    // TEST ERRORS
    // -----------

    // Will fail due technical error (like connection error)
    db().close()

    val (retKO, emptyList) = retrieveOrganization(existingOrgName, db, logger)
    assertTrue(retKO is SQLReturnCode.DBError, "Return should be DBError")
    assertEquals("Unable to recover organization '$existingOrgName' due an internal error", retKO.message)
    assertTrue(emptyList.isEmpty(), "Query should return no results")
  }

  @Test
  fun testDeleteOrganization() {
    assertNotNull(db(), "Unable to execute test due an error with database connection")

    // Prepare data
    val (retPrep1, newObj1) =
        insertOrganization(Organization(0, "__delete_me__", 1), db, logger)
    assertEquals(SQLReturnCode.Inserted, retPrep1, "Unable to execute test due an error while preparing data for testing")
    assertNotNull(newObj1, "Unable to execute test due an error while preparing data for testing")

    val (retPrep2, newObj2) =
        insertOrganization(Organization(0, "__for_having_children__", 1), db, logger)
    assertEquals(SQLReturnCode.Inserted, retPrep2, "Unable to execute test due an error while preparing data for testing")
    assertNotNull(newObj2, "Unable to execute test due an error while preparing data for testing")

    // Delete object
    val (retOK, deletedObj) = deleteOrganization(newObj1.id, db, logger)
    assertEquals(SQLReturnCode.Deleted, retOK, "Deletion of object should return DELETED")
    assertEquals(newObj1, deletedObj)

    // Check results
    val (retQry, listFromDB) = retrieveOrganization(newObj1.id, db, logger)
    assertTrue(retQry is SQLReturnCode.Succeeded, "Unable to ensure the success of test due and error checking results")
    assertTrue(listFromDB.isEmpty(), "Deleted object shouldn't exist in database anymore")

    // delete unexisting object (previously it has been deleted)
    val (retOK2, deletedObj2) = deleteOrganization(newObj1.id, db, logger)
    assertEquals(SQLReturnCode.Unaltered, retOK2)
    assertNull(deletedObj2, "No object should be returned in an UNALTERED delete operation")

    // -----------
    // TEST ERRORS
    // -----------

    // Organization is not empty: Will fail due foreign key constraint violation
    // We create a fake child table with a fake constraint
    // - CONSTRAINT would probably be named like: 'child_organizations_FK'
    // - and error message would be similar to: 'organization is not empty'
    // but, since this constraint is not actually defined for organzations table, the obtained error will be
    // the generic 'Integrity constraint violation', until we have some dependent tables
    val connection = db()
    connection.createStatement().execute("""
      |CREATE TABLE child (
      |  `id` int,
      |  `org` int,
      |  CONSTRAINT child_organizations_FK FOREIGN KEY (org)
      |    REFERENCES organizations(id)
      |    ON UPDATE CASCADE
      |    ON DELETE NO ACTION
      |);
      |""".trimMargin().trim())
    connection.createStatement().execute("INSERT INTO child (id, org) VALUES (1, ${newObj2.id})")
    val expectedErrChild = "Integrity constraint violation"

    val (retChild, deletedObjKOChild) = deleteOrganization(newObj2.id, db, logger)
    assertTrue(retChild is SQLReturnCode.ConstraintError, "Return should be ConstraintError")
    assertEquals(expectedErrChild, retChild.message)
    assertEquals(newObj2, deletedObjKOChild) // Not actually deleted but found and failed to delete

    // Will fail due technical error (like connection error)
    connection.close()
    val expectedErrKO = "Unable to access organization id='${newObj2.id}' for deleting it"

    val (retKO, deletedObjKO) = deleteOrganization(newObj2.id, db, logger)
    assertTrue(retKO is SQLReturnCode.DBError, "Return should be DBError")
    assertEquals(expectedErrKO, retKO.message)
    assertNull(deletedObjKO, "Statement shouldn't delete any record")
  }

  @Test
  fun testInsertOrganization() {
    assertNotNull(db(), "Unable to execute test due an error with database connection")

    val existingOwner = 1
    val unexistingOwner = 0

    val orgToInsertOnce = Organization(0, "org1_${Date().time}", existingOwner)
    val orgToFail       = Organization(0, "org2_${Date().time}", unexistingOwner)
    val orgToFailByConn = Organization(0, "org3_${Date().time}", existingOwner)

    // Will insert NEW record
    val (ret, newObj) = insertOrganization(orgToInsertOnce, db, logger)
    assertEquals(SQLReturnCode.Inserted, ret)
    assertNotNull(newObj, "Inserted object shouldn't be null")
    assertTrue(newObj.id != 0, "New object should have a generated id")

    // -----------
    // TEST ERRORS
    // -----------

    // Name already taken: Will fail due unique key violation
    val (retDupKey, newObjDupKey) = insertOrganization(orgToInsertOnce, db, logger)
    assertTrue(retDupKey is SQLReturnCode.UniqueError, "Return should be UniqueError")
    assertEquals("Name already taken", retDupKey.message)
    assertNull(newObjDupKey, "Inserted object should be null on insertion error")

    // Owner not found: Will fail due foreign key constraint violation
    val (retBadFK, newObjBadFK) = insertOrganization(orgToFail, db, logger)
    assertTrue(retBadFK is SQLReturnCode.FKError, "Return should be FKError")
    assertEquals("Owner not found", retBadFK.message)
    assertNull(newObjBadFK, "Inserted object should be null on insertion error")

    // Will fail due technical error (like connection error)
    db().close()

    val (retConnErr, newObjConnErr) = insertOrganization(orgToFailByConn, db, logger)
    assertTrue(retConnErr is SQLReturnCode.DBError, "Return should be DBError")
    assertEquals("Unable to persist organization '${orgToFailByConn.name}' due an internal error", retConnErr.message)
    assertNull(newObjConnErr, "Inserted object should be null on insertion error")
  }

  @Test
  fun testUpdateOrganization() {
    assertNotNull(db(), "Unable to execute test due an error with database connection")

    val existingOwner = 1
    val unexistingOwner = 0
    val unexistingOrg = 0

    // Prepare data
    val (retPrep1, orgToLockName) =
        insertOrganization(Organization(0, "locked_name", existingOwner), db, logger)
    assertEquals(SQLReturnCode.Inserted, retPrep1, "Unable to execute test due an error while preparing data for testing")
    assertNotNull(orgToLockName, "Unable to execute test due an error while preparing data for testing")

    val (retPrep2, orgToUpdate) =
        insertOrganization(Organization(0, "org1_${Date().time}", existingOwner), db, logger)
    assertEquals(SQLReturnCode.Inserted, retPrep2, "Unable to execute test due an error while preparing data for testing")
    assertNotNull(orgToUpdate, "Unable to execute test due an error while preparing data for testing")

    val (retPrep3, orgToFailUpdating) =
        insertOrganization(Organization(0, "__original_name__", existingOwner), db, logger)
    assertEquals(SQLReturnCode.Inserted, retPrep3, "Unable to execute test due an error while preparing data for testing")
    assertNotNull(orgToFailUpdating, "Unable to execute test due an error while preparing data for testing")

    // Update object
    val retOK = updateOrganization(orgToUpdate.copy(name = "__updated__"), db, logger)
    assertEquals(SQLReturnCode.Updated, retOK, "Update of object should return UPDATED")

    val retNotFound = updateOrganization(orgToUpdate.copy(id = unexistingOrg), db, logger)
    assertEquals(SQLReturnCode.NotFound, retNotFound, "Update of object should return NOT_FOUND")

    // Check results
    val (retQry, listFromDB) = retrieveOrganization(orgToUpdate.id, db, logger)
    assertTrue(retQry is SQLReturnCode.Succeeded, "Unable to ensure the success of test due and error checking results")
    assertEquals(1, listFromDB.size, "Updated object should exist in database")
    assertEquals("__updated__", listFromDB[0].name)

    // -----------
    // TEST ERRORS
    // -----------

    // Name already taken: Will fail due unique key violation
    val retDupKey = updateOrganization(orgToFailUpdating.copy(name = "locked_name"), db, logger)
    assertTrue(retDupKey is SQLReturnCode.UniqueError, "Return should be UniqueError")
    assertEquals("Name already taken", retDupKey.message)
    // Check results (original record remains unchanged)
    val (retCheckDupKey, orgsCheckDupKey) = retrieveOrganization(orgToFailUpdating.id, db, logger)
    assertTrue(retCheckDupKey is SQLReturnCode.Succeeded, "Unable to ensure the success of test due and error checking results")
    assertEquals(1, orgsCheckDupKey.size, "Original object should be unaltered in database")
    assertEquals(orgToFailUpdating.name, orgsCheckDupKey[0].name)

    // Owner not found: Will fail due foreign key constraint violation
    val retBadFK = updateOrganization(orgToFailUpdating.copy(owner = unexistingOwner), db, logger)
    assertTrue(retBadFK is SQLReturnCode.FKError, "Return should be FKError")
    assertEquals("Owner not found", retBadFK.message)
    // Check results (original record remains unchanged)
    val (retCheckBadFK, orgsCheckBadFK) = retrieveOrganization(orgToFailUpdating.id, db, logger)
    assertTrue(retCheckBadFK is SQLReturnCode.Succeeded, "Unable to ensure the success of test due and error checking results")
    assertEquals(1, orgsCheckBadFK.size, "Original object should be unaltered in database")
    assertEquals(orgToFailUpdating.name, orgsCheckBadFK[0].name)

    // Will fail due technical error (like connection error)
    db().close()

    val retConnErr = updateOrganization(orgToFailUpdating.copy(name = "name_you_wont_see"), db, logger)
    assertTrue(retConnErr is SQLReturnCode.DBError, "Return should be DBError")
    assertEquals("Unable to update organization id='${orgToFailUpdating.id}' due an internal error", retConnErr.message)
  }
}
