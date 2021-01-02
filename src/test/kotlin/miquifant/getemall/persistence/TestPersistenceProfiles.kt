/**
 * Test persistence functions for Profiles.
 *
 * Created by miquifant on 2020-12-29
 */
package miquifant.getemall.persistence

import miquifant.getemall.log.Loggable.Logger
import miquifant.getemall.log.LoggerFactory
import miquifant.getemall.testingutils.initDatabaseConnection
import miquifant.getemall.utils.ConnectionManager

import java.util.*

import kotlin.test.*


class TestPersistenceProfiles {

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
  fun testRetrieveProfilesList() {
    assertNotNull(db(), "Unable to execute test due an error with database connection")

    val (ret, profiles) = retrieveProfilesList(db, logger)
    assertEquals(SQLReturnCode.Succeeded, ret)
    assertEquals(4, profiles.size)

    val connection = db()
    // Since there is a constraint in organizations table, we have to empty that table first
    connection.createStatement().execute("TRUNCATE TABLE organizations")
    connection.createStatement().execute("DELETE FROM users")
    val (retEmpty, noProfiles) = retrieveProfilesList(db, logger)
    assertEquals(SQLReturnCode.Succeeded, retEmpty)
    assertEquals(0, noProfiles.size)

    // -----------
    // TEST ERRORS
    // -----------

    // Will fail due technical error (like connection error)
    connection.close()

    val (retKO, emptyList) = retrieveProfilesList(db, logger)
    assertTrue(retKO is SQLReturnCode.DBError, "Query should fail")
    assertEquals("Unable to recover profiles list due an internal error", retKO.message)
    assertTrue(emptyList.isEmpty(), "Query should return no results")
  }

  @Test
  fun testRetrieveProfileById() {
    assertNotNull(db(), "Unable to execute test due an error with database connection")

    val existingProfile = 1
    val unexistingProfile = 0

    val (ret1, profilesWithExistingId) = retrieveProfile(existingProfile, db, logger)
    assertEquals(SQLReturnCode.Succeeded, ret1)
    assertEquals(1, profilesWithExistingId.size)

    val (ret2, profilesWithUnexistingId) = retrieveProfile(unexistingProfile, db, logger)
    assertEquals(SQLReturnCode.Succeeded, ret2)
    assertEquals(0, profilesWithUnexistingId.size)

    // -----------
    // TEST ERRORS
    // -----------

    // Will fail due technical error (like connection error)
    db().close()

    val (retKO, emptyList) = retrieveProfile(existingProfile, db, logger)
    assertTrue(retKO is SQLReturnCode.DBError, "Return should be DBError")
    assertEquals("Unable to recover profile id='$existingProfile' due an internal error", retKO.message)
    assertTrue(emptyList.isEmpty(), "Query should return no results")
  }

  @Test
  fun testRetrieveProfileByName() {
    assertNotNull(db(), "Unable to execute test due an error with database connection")

    val existingProfileName = "miqui"
    val unexistingProfileName = "unexisting"

    val (ret1, profilesWithExistingName) = retrieveProfile(existingProfileName, db, logger)
    assertEquals(SQLReturnCode.Succeeded, ret1)
    assertEquals(1, profilesWithExistingName.size)

    val (ret2, profilesWithUnexistingName) = retrieveProfile(unexistingProfileName, db, logger)
    assertEquals(SQLReturnCode.Succeeded, ret2)
    assertEquals(0, profilesWithUnexistingName.size)

    // -----------
    // TEST ERRORS
    // -----------

    // Will fail due technical error (like connection error)
    db().close()

    val (retKO, emptyList) = retrieveProfile(existingProfileName, db, logger)
    assertTrue(retKO is SQLReturnCode.DBError, "Return should be DBError")
    assertEquals("Unable to recover profile '$existingProfileName' due an internal error", retKO.message)
    assertTrue(emptyList.isEmpty(), "Query should return no results")
  }
}
