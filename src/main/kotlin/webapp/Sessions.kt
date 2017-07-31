import com.google.gson.Gson
import spark.Request
import spark.Response
import spark.Spark
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.SecureRandom
import java.util.Base64

const val COOKIE_NAME = "todo-kotlin"
const val CSRF_VALUE_LENGTH = 24

val secureRandom = SecureRandom.getInstanceStrong()!!

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
  if (cookieValue != null) {
    try {
      val session = Gson().fromJson(URLDecoder.decode(cookieValue, "UTF-8"),
          Session::class.java)
      return session
    } catch (e: com.google.gson.JsonSyntaxException) {
      return Session(null, null, null)
    }
  } else {
    return Session(null, null, null)
  }
}

fun updateSession(oldSession: Session, res: Response): Session {
  val bytes = ByteArray(CSRF_VALUE_LENGTH * 6 / 8)
  secureRandom.nextBytes(bytes)
  val newCsrfValue = Base64.getEncoder().encodeToString(bytes)
  val newSession = oldSession.setCsrfValue(newCsrfValue)

  res.cookie(COOKIE_NAME,
      URLEncoder.encode(Gson().toJson(newSession), "UTF-8"))

  return newSession
}
