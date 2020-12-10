/**
 * Test connection to database.
 *
 * Created by miquifant on 2020-11-14
 */
package miquifant.getemall.utils

import miquifant.getemall.utils.ConnectionParams.PASSWORD
import miquifant.getemall.utils.ConnectionParams.SCHEMA
import miquifant.getemall.utils.ConnectionParams.URL
import miquifant.getemall.utils.ConnectionParams.USER

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory

import kotlin.test.*


class TestDBConnectionManager {

  private val tmpDatabase = "db_${java.util.Date().time}"
  private lateinit var config: Config

  @BeforeTest
  fun setup() {
    config = ConfigFactory.parseMap(mapOf(
        URL      to "jdbc:h2:mem:$tmpDatabase",
        USER     to "dbuser",
        PASSWORD to "dbpassword",
        SCHEMA   to "PUBLIC"
    ))
  }

  @Test
  fun testConnection() {
    val db = connectionManager(config)
    val connection = db()

    assertNotNull(connection, "Connection shouldn't be null")
    assertEquals("PUBLIC", connection.schema)

    connection.close()
  }

  @Test(expected = IllegalStateException::class)
  fun testFailedSchemaConnection() {
    val wrongConfig = config.withValue(SCHEMA, ConfigValueFactory.fromAnyRef("WRONG"))
    val db = connectionManager(wrongConfig)
    db()
  }

  @Test
  fun testConnectionManager() {
    val db = connectionManager(config)

    val connection1 = db()
    val connection2 = db()

    assertNotNull(connection1, "Connection shouldn't be null")
    assertNotNull(connection2, "Connection shouldn't be null")
    assertSame(connection1, connection2, "Connections should be the same")

    connection1.close()
  }

  @Test
  fun testConnectionManagerReconnect() {
    val db = connectionManager(config)

    val connection1 = db()
    assertNotNull(connection1, "Connection shouldn't be null")
    connection1.close()

    val connection2 = db()
    assertNotNull(connection2, "Connection shouldn't be null")
    assertNotSame(connection1, connection2, "Connection shouldn't be the same")
    connection2.close()
  }
}
