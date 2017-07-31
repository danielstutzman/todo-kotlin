package webapp

import com.google.gson.Gson
import spark.Request
import spark.Response
import spark.Spark
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

const val COOKIE_NAME = "todo-kotlin"
const val CSRF_VALUE_LENGTH = 24
const val HMAC_SHA1_ALGORITHM = "HmacSHA1";

val EMPTY_SESSION = Session(null, null, null)

data class Session(
    val userId: Int?,
    val csrfValue: String?,
    val flashNotice: String?
) {
  fun setUserId(newUserId: Int?) = Session(
      newUserId,
      csrfValue,
      flashNotice
  )

  fun setCsrfValue(newCsrfValue: String) = Session(
      userId,
      newCsrfValue,
      flashNotice
  )

  fun setFlashNotice(newFlashNotice: String) = Session(
      userId,
      csrfValue,
      newFlashNotice
  )
}

data class SessionStorage(val secret: String) {
  val secureRandom = SecureRandom.getInstanceStrong()!!
  fun loadSessionAndCheckCsrf(req: Request): Session {
    val session = loadSession(req)

    if (req.requestMethod() != "GET") {
      if (session.csrfValue == null) {
        throw RuntimeException("Expected non-null session.csrfValue")
      }
      if (req.queryParams("authenticity_token") != session.csrfValue) {
        println("Expected csrfValue ${session.csrfValue} but got ${req.queryParams("authenticity_token")}")
        throw Spark.halt(401,
            "Expected csrfValue ${session.csrfValue} but got ${req.queryParams("authenticity_token")}")
      }
    }

    return session
  }

  private fun loadSession(req: Request): Session {
    val cookieValue = req.cookie(COOKIE_NAME)
    if (cookieValue == null) {
      println("LOAD SESSION: found no cookievalue")
      return EMPTY_SESSION
    }

    val parts = cookieValue.split("|")
    if (parts.size != 2) {
      println("LOAD SESSION: wrong num parts")
      return EMPTY_SESSION
    }

    val sessionSerialized = try {
      URLDecoder.decode(parts[0], "UTF-8")
    } catch (e: java.lang.IllegalArgumentException) {
      println("LOAD SESSION: Couldn't url encode")
      return EMPTY_SESSION
    }
    val oldHmac = parts[1]

    val mac = Mac.getInstance(HMAC_SHA1_ALGORITHM)
    val signingKey = SecretKeySpec(secret.toByteArray(), HMAC_SHA1_ALGORITHM)
    mac.init(signingKey)
    val newHmac = String(Base64.getUrlEncoder().encode(
        mac.doFinal(sessionSerialized.toByteArray()))).replace("=", "")

    if (oldHmac != newHmac) {
      println("LOAD SESSION: Expected HMAC ${oldHmac} but got ${newHmac}")
      return EMPTY_SESSION
    }

    try {
      val session = Gson().fromJson(
          URLDecoder.decode(sessionSerialized, "UTF-8"),
          Session::class.java)
      return session
    } catch (e: com.google.gson.JsonSyntaxException) {
      return EMPTY_SESSION
    }
  }

  fun updateSession(oldSession: Session, res: Response): Session {
    val bytes = ByteArray(CSRF_VALUE_LENGTH * 6 / 8)
    secureRandom.nextBytes(bytes)
    val newCsrfValue = Base64.getUrlEncoder().encodeToString(bytes)
    val newSession = oldSession.setCsrfValue(newCsrfValue)
    val sessionSerialized = Gson().toJson(newSession)

    val mac = Mac.getInstance(HMAC_SHA1_ALGORITHM)
    val signingKey = SecretKeySpec(secret.toByteArray(), HMAC_SHA1_ALGORITHM)
    mac.init(signingKey)
    val hmac = String(Base64.getUrlEncoder().encode(
        mac.doFinal(sessionSerialized.toByteArray()))).replace("=", "")

    val signed = URLEncoder.encode(sessionSerialized, "UTF-8") + "|" +
        hmac
    res.cookie(COOKIE_NAME, signed)

    return newSession
  }
}
