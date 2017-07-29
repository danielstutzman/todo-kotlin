package appPkg

import java.security.SecureRandom
import java.util.*

class SecureTokenGenerator(val tokenSize: Int) {
  private val random = SecureRandom()

  fun nextToken(): String {
    val numBytes = this.tokenSize * 6 / 8
    val bytes: ByteArray = ByteArray(numBytes).apply {
      random.nextBytes(this)
    }
    val base64Bytes = Base64.getEncoder().encodeToString(bytes)
    return base64Bytes
        .replace('l', 's')
        .replace('I', 'x')
        .replace('O', 'y')
        .replace('0', 'z')
  }
}