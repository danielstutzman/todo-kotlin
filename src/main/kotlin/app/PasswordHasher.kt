package app

import org.mindrot.jbcrypt.BCrypt
import webapp.ReqLog

interface PasswordHasher {
  fun hash(password: String): String
  fun matches(password: String, passwordHash: String): Boolean
}

class SecurePasswordHasher(val strength: Int) : PasswordHasher {
  override fun hash(password: String): String {
    ReqLog.start()
    try {
      return BCrypt.hashpw(password, BCrypt.gensalt(strength))
    } finally {
      ReqLog.finish()
    }
  }

  override fun matches(password: String, passwordHash: String): Boolean {
    ReqLog.start()
    try {
      return BCrypt.checkpw(password, passwordHash)
    } finally {
      ReqLog.finish()
    }
  }
}

class FakePasswordHasher() : PasswordHasher {
  override fun hash(password: String) = password.toUpperCase()

  override fun matches(password: String, passwordHash: String) =
      password == passwordHash.toUpperCase()
}