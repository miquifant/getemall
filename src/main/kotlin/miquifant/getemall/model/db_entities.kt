@file:JvmName("DBEntities")
/**
 * Data classes with DB entities in getemall schema.
 *
 * Created by miquifant on 2020-11-09
 */
package miquifant.getemall.model

import java.sql.Timestamp


data class ProfileExt(val profilePic: String? = null,
                      val fullName: String? = null,
                      val pubEmail: String? = null,
                      val pubEmailVerified: Boolean = false,
                      val bio: String? = null)

data class Profile(val id: Int,
                   val email: String,
                   val name: String,
                   val role: Int,
                   val timestamp: Timestamp,
                   val verified: Boolean,
                   val active: Boolean,
                   val ext: ProfileExt = ProfileExt())
