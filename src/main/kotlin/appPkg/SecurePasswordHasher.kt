package appPkg

import org.mindrot.jbcrypt.BCrypt

class SecurePasswordHasher(val strength: Int) {
  fun hash(password: String) =
      BCrypt.hashpw(password, BCrypt.gensalt(strength))

  fun matches(password: String, passwordHash: String) =
      BCrypt.checkpw(password, passwordHash)
}