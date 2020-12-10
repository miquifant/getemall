@file:JvmName("DBTestingUtils")
/**
 * db module for database related functions, used during tests.
 *
 * Created by miquifant on 2020-11-14
 */
package miquifant.getemall.testingutils

import miquifant.getemall.utils.ConnectionManager
import miquifant.getemall.utils.connectionManager
import miquifant.getemall.utils.ConnectionParams.PASSWORD
import miquifant.getemall.utils.ConnectionParams.SCHEMA
import miquifant.getemall.utils.ConnectionParams.URL
import miquifant.getemall.utils.ConnectionParams.USER

import com.typesafe.config.ConfigFactory

import java.io.File


private const val SCHEMA_DEFINITION_FILE = "database/sql/getemall_schema.sql"
private const val TEST_DATA_FILE         = "database/sql/getemall_test_data.sql"

/**
 * Creates a ConnectionManager for connection to an ad-hoc created database.
 * It uses slightly modified versions of scripts: schema.sql and test_data.sql.
 */
fun initDatabaseConnection(tmpDatabase: String): ConnectionManager {
  val db = connectionManager(ConfigFactory.parseMap(mapOf(
      URL      to "jdbc:h2:mem:$tmpDatabase;MODE=MYSQL;DATABASE_TO_LOWER=TRUE;IGNORECASE=TRUE",
      USER     to "dbuser",
      PASSWORD to "dbpassword",
      SCHEMA   to "public"
  )))
  val connection = db()
  val schema = "getemall"

  val createSchemaStmt = connection.createStatement()
  createSchemaStmt.execute("CREATE SCHEMA $schema")
  createSchemaStmt.closeOnCompletion()

  connection.schema = schema

  val statement = connection.createStatement()

  val createSQL   = File(SCHEMA_DEFINITION_FILE)
  val testDataSQL = File(TEST_DATA_FILE)

  val ddl: String = createSQL.readText()
      // Remove clauses non valid syntax for H2
      .replace(Regex("ENGINE=.*"), "")
      .replace(Regex("CHARACTER SET .*"), "")
      .replace("DEFAULT nickname", "")        // Can't test with H2 the assignment of default values from other columns.

  val testData: String = testDataSQL.readText()
      // In H2 if you write down 4 slashes, then 4 slashes will be inserted, not like in MySQL
      .replace("\\\\", "\\")

  statement.execute(ddl)
  statement.execute(testData)

  statement.closeOnCompletion()

  return db
}
