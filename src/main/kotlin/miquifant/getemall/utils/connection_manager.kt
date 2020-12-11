@file:JvmName("DBConnectionManager")
/**
 * Getemall DB Connection Manager API
 *
 * Created by miquifant on 2020-11-14
 */
package miquifant.getemall.utils

import miquifant.getemall.utils.ConnectionParams.PASSWORD
import miquifant.getemall.utils.ConnectionParams.SCHEMA
import miquifant.getemall.utils.ConnectionParams.URL
import miquifant.getemall.utils.ConnectionParams.USER

import com.typesafe.config.Config

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.SQLTimeoutException


typealias ConnectionManager = () -> Connection

const val ONE_SECOND_TIMEOUT = 1

object ConnectionParams {
  const val URL      = "db.url"
  const val USER     = "db.user"
  const val PASSWORD = "db.password"
  const val SCHEMA   = "db.schema"
}

private fun connectToDB(config: Config): Connection =
    withConnectExceptionsControlled {
      val url      = config.getString(URL)
      val user     = config.getString(USER)
      val password = config.getString(PASSWORD)
      val dbschema = config.getString(SCHEMA)
      DriverManager.getConnection(url, user, password).apply {
        schema = dbschema
        autoCommit = true
      }
    }

private fun withConnectExceptionsControlled(block: () -> Connection) =
    try {
      block()
    }
    catch (e: SQLException) {
      throw IllegalStateException("Not able to connect to database, please review configuration file", e)
    }
    catch (e: SQLTimeoutException) {
      throw IllegalStateException("Not able to connect to database because we got a timeout", e)
    }
    catch (e: Throwable) {
      throw IllegalStateException("Not able to connect to database, please review configuration file", e)
    }

fun connectionManager(config: Config): ConnectionManager {
  var db: Connection? = null
  return {
    if (db == null || !db!!.isValid(ONE_SECOND_TIMEOUT)) db = connectToDB(config)
    db!!
  }
}
