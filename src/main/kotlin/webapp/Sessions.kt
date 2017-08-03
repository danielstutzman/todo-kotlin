package webapp

import com.google.gson.Gson
import org.slf4j.LoggerFactory
import spark.Request
import spark.Response
import spark.Spark
import views.FlashMessage
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

const val COOKIE_NAME = "todo-kotlin"
const val CSRF_VALUE_LENGTH = 24
const val HMAC_SHA1_ALGORITHM = "HmacSHA1";

data class Session(
    val userId: Int?,
    val csrfValue: String?,
    val flash: FlashMessage?
) {
  constructor() : this(null, null, null) {}
}

data class SessionStorage(val secret: String) {
  private val secureRandom = SecureRandom.getInstanceStrong()!!
  private val logger = LoggerFactory.getLogger(SessionStorage::class.java)

  fun loadSessionAndCheckCsrf(req: Request): Session {
    ReqLog.start()
    try {
      val session = loadSession(req)

      if (req.requestMethod() != "GET") {
        if (session.csrfValue == null) {
          throw RuntimeException("Expected non-null session.csrfValue")
        }
        if (req.queryParams("authenticity_token") != session.csrfValue) {
          logger.warn("Expected csrfValue ${session.csrfValue} but got ${req.queryParams("authenticity_token")}")
          throw Spark.halt(401,
              "Expected csrfValue ${session.csrfValue} but got ${req.queryParams("authenticity_token")}")
        }
      }

      return session
    } finally {
      ReqLog.finish()
    }
  }

  private fun loadSession(req: Request): Session {
    val cookieValue = req.cookie(COOKIE_NAME)
    if (cookieValue == null) {
      logger.info("method=load_session error=no_cookie_value")
      return Session()
    }

    val parts = cookieValue.split("|")

    if (parts.size != 2) {
      logger.warn("method=load_session cookieValue=\"${cookieValue}\" error=wrong_num_parts")
      return Session()
    }

    val sessionSerialized = try {
      URLDecoder.decode(parts[0], "UTF-8")
    } catch (e: java.lang.IllegalArgumentException) {
      logger.warn("method=load_session cookieValue=\"${cookieValue}\" error=couldnt_url_decode")
      return Session()
    }
    val oldHmac = parts[1]

    val mac = Mac.getInstance(HMAC_SHA1_ALGORITHM)
    val signingKey = SecretKeySpec(secret.toByteArray(), HMAC_SHA1_ALGORITHM)
    mac.init(signingKey)
    val newHmac = String(Base64.getUrlEncoder().encode(
        mac.doFinal(sessionSerialized.toByteArray()))).replace("=", "")

    if (oldHmac != newHmac) {
      logger.warn("method=load_session cookieValue=\"${cookieValue}\" expected_hmac=\"${oldHmac}\" actual_hmac=\"${newHmac}\" error=wrong_hmac")
      return Session()
    }

    try {
      val session = Gson().fromJson(
          URLDecoder.decode(sessionSerialized, "UTF-8"),
          Session::class.java)
      ReqLog.setUserId(session.userId)
      return session
    } catch (e: com.google.gson.JsonSyntaxException) {
      logger.warn("method=load_session cookieValue=\"${cookieValue}\" exception=${e.message} error=bad_json")
      return Session()
    }
  }

  fun updateSession(oldSession: Session, res: Response): Session {
    ReqLog.start()
    try {
      ReqLog.setUserId(oldSession.userId)

      val bytes = ByteArray(CSRF_VALUE_LENGTH * 6 / 8)
      secureRandom.nextBytes(bytes)
      val newCsrfValue = Base64.getUrlEncoder().encodeToString(bytes)
      val newSession = oldSession.copy(csrfValue = newCsrfValue)
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
    } finally {
      ReqLog.finish()
    }
  }
}
