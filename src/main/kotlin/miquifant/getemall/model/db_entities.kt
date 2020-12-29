@file:JvmName("DBEntities")
/**
 * Data classes with DB entities in getemall schema.
 *
 * Created by miquifant on 2020-11-09
 */
package miquifant.getemall.model

import java.sql.Timestamp


data class Profile(val id: Int,
                   val email: String,
                   val name: String,
                   val fullname: String,
                   val role: Int,
                   val timestamp: Timestamp,
                   val verified: Boolean,
                   val active: Boolean)
