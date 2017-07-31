package app

import org.mindrot.jbcrypt.BCrypt

interface PasswordHasher {
  fun hash(password: String): String
  fun matches(password: String, passwordHash: String): Boolean
}

class SecurePasswordHasher(val strength: Int) : PasswordHasher {
  override fun hash(password: String) =
      BCrypt.hashpw(password, BCrypt.gensalt(strength))

  override fun matches(password: String, passwordHash: String) =
      BCrypt.checkpw(password, passwordHash)
}

class FakePasswordHasher() : PasswordHasher {
  override fun hash(password: String) = password.toUpperCase()

  override fun matches(password: String, passwordHash: String) =
      password == passwordHash.toUpperCase()
}