/**
 * Users Data access class, based on a in-memory list.
 *
 * Created by miquifant on 2020-10-25
 */
package miquifant.getemall.api.authentication.impl

import miquifant.getemall.api.authentication.User
import miquifant.getemall.api.authentication.UserDao
import miquifant.getemall.utils.AppRole.ADMIN
import miquifant.getemall.utils.AppRole.REGULAR_USER

import org.mindrot.jbcrypt.BCrypt


class UserListDao: UserDao {

  private val users = listOf (
      //   Username  Salt for hash                     Hashed password (the password is "password" for all users)
      User("miqui",  "$2a$10\$h.dl5J86rGH7I8bD9bZeZe", "$2a$10\$h.dl5J86rGH7I8bD9bZeZeci0pDt0.VwFTGujlnEaZXPf/q7vM5wO", ADMIN),
      User("esther", "$2a$10\$e0MYzXyjpJS7Pd0RVvHwHe", "$2a$10\$e0MYzXyjpJS7Pd0RVvHwHe1HlCS4bZJ18JuywdEMLT83E1KDmUhCy", REGULAR_USER),
      User("ramon",  "$2a$10\$E3DgchtVry3qlYlzJCsyxe", "$2a$10\$E3DgchtVry3qlYlzJCsyxeSK0fftK4v0ynetVCuDdxGVl1obL.ln2", REGULAR_USER)
  )
  override fun getUserByUsername(username: String): User? {
    return users.stream().filter { b -> b.name == username }.findFirst().orElse(null)
  }
  /**
   * Authenticate the user by hashing the inputted password using the stored salt,
   * then comparing the generated hashed password to the stored hashed password.
   */
  override fun authenticate(username: String?, password: String?): User? {
    return if (username == null || password == null) null
    else if (username == "admin" && password == "admin") User(username, "", "", ADMIN)
    else {
      val user = getUserByUsername(username)
      when {
        user == null -> null
        BCrypt.hashpw(password, user.salt) == user.hashedPass -> user
        else -> null
      }
    }
  }
}
