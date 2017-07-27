import org.cyberneko.html.parsers.SAXParser
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.xml.sax.InputSource
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*

class HelloTest {
  companion object {
    @JvmStatic public fun main(args: Array<String>) {
      HelloTest().testCapitalize()
    }
  }

  @Before fun setUp() {
  }

  @After fun tearDown() {
  }

  fun getForCookiesAndAuthToken(url: URL): Pair<List<String>, String> {
    val http = url.openConnection() as HttpURLConnection

    try {
      http.connect()
    } catch (e: java.net.ConnectException) {
      throw RuntimeException("Couldn't connect to ${url}", e)
    }

    if (http.responseCode != HttpURLConnection.HTTP_OK) {
      throw RuntimeException("Got ${http.responseCode} from ${url}")
    }

    val cookies: List<String>? = http.headerFields.get("Set-Cookie")
    if (cookies == null) {
      throw NullPointerException("Can't find Set-Cookie header")
    }

    val parser = SAXParser()
    val findToken = SAXFindAuthenticityToken()
    parser.contentHandler = findToken
    parser.parse(InputSource(http.inputStream))
    val foundToken = findToken.foundToken
    if (foundToken == null) {
      throw NullPointerException("Can't find authenticity_token")
    }

    return Pair(cookies, foundToken)
  }

  /** @return body of POST (might follow redirect) */
  fun post(url: URL, cookies: List<String>, authToken: String): String {
    val http = url.openConnection() as HttpURLConnection
    http.requestMethod = "POST"
    http.doOutput = true
    for (cookie in cookies) {
      http.addRequestProperty("Cookie", cookie.split(";".toRegex(), 2)[0])
    }

    val params = HashMap<String, String>()
    params.put("user[email]", "a@a.com")
    params.put("user[password]", "password")
    params.put("authenticity_token", authToken)

    val joiner = StringJoiner("&")
    for ((key, value) in params) {
      joiner.add(URLEncoder.encode(key, "UTF-8")
          + "="
          + URLEncoder.encode(value, "UTF-8"))
    }

    val paramsBytes = joiner.toString().toByteArray()
    http.setRequestProperty("Content-Type",
        "application/x-www-form-urlencoded; charset=UTF-8")
    http.connect()
    http.outputStream.use { outStream ->
      outStream.write(paramsBytes)
    }

    val body = http.inputStream.bufferedReader().use { it.readText() }
    return body
  }

  @Test fun testCapitalize() {
    val (cookies, authToken) =
        getForCookiesAndAuthToken(URL("http://localhost:3000/users/sign_in"))
    val body = post(URL("http://localhost:3000/users/sign_in"), cookies, authToken)
    File("sign_in_success.html").writeText(body)
  }
}
