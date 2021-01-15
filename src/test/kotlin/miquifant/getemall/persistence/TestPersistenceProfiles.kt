/**
 * Test persistence functions for Profiles.
 *
 * Created by miquifant on 2020-12-29
 */
package miquifant.getemall.persistence

import miquifant.getemall.log.Loggable.Logger
import miquifant.getemall.log.LoggerFactory
import miquifant.getemall.model.ProfileExt
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

  @Test
  fun testCheckUsernameAvailability() {
    assertNotNull(db(), "Unable to execute test due an error with database connection")

    val availableUsername = "unexisting"
    val takenUsername = "miqui"

    val (ret1, availableWithUnexistingUsername) = checkUsernameAvailability(availableUsername, db, logger)
    assertEquals(SQLReturnCode.Succeeded, ret1)
    assertTrue(availableWithUnexistingUsername, "Username should be available")

    val (ret2, availableWithExistingUsername) = checkUsernameAvailability(takenUsername, db, logger)
    assertEquals(SQLReturnCode.Succeeded, ret2)
    assertFalse(availableWithExistingUsername, "Username shouldn't be available")

    // -----------
    // TEST ERRORS
    // -----------

    // Will fail due technical error (like connection error)
    db().close()

    val (retKO, alwaysFalse) = checkUsernameAvailability(availableUsername, db, logger)
    assertTrue(retKO is SQLReturnCode.DBError, "Return should be DBError")
    assertEquals("Unable to check Username availability due an internal error", retKO.message)
    assertFalse(alwaysFalse, "It should return username is not available")
  }

  @Test
  fun testPatchProfileUsername() {
    assertNotNull(db(), "Unable to execute test due an error with database connection")

    val goodCurrentName1 = "esther"
    val goodNewNameForName1 = "whatever"

    val wrongCurrentName2 = "unexisting"
    val goodNewNameForName2 = "boss"
    val goodCurrentName2 = "miqui"
    val wrongNewNameForName2 = "ramon"

    val goodCurrentName3 = "ramon"
    val goodNewNameForName3 = "god"

    // Rename to the same name (left unchanged) returns PATCHED, as if it was changed
    val retUnchanged = patchProfileUsername(goodCurrentName1, goodCurrentName1, db, logger)
    assertEquals(SQLReturnCode.Patched, retUnchanged)

    val retChanged = patchProfileUsername(goodCurrentName1, goodNewNameForName1, db, logger)
    assertEquals(SQLReturnCode.Patched, retChanged)

    val retNotFound = patchProfileUsername(wrongCurrentName2, goodNewNameForName2, db, logger)
    assertEquals(SQLReturnCode.NotFound, retNotFound)

    // -----------
    // TEST ERRORS
    // -----------

    // Name already taken: Will fail due unique key violation
    val retDupKey = patchProfileUsername(goodCurrentName2, wrongNewNameForName2, db, logger)
    assertTrue(retDupKey is SQLReturnCode.UniqueError, "Return should be UniqueError")
    assertEquals("Username already taken", retDupKey.message)

    // Will fail due technical error (like connection error)
    db().close()

    val retConnErr = patchProfileUsername(goodCurrentName3, goodNewNameForName3, db , logger)
    assertTrue(retConnErr is SQLReturnCode.DBError, "Return should be DBError")
    assertEquals("Unable to patch profile '$goodCurrentName3' due an internal error", retConnErr.message)
  }
}
