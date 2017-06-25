import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.StringJoiner
import java.util.HashMap
import kotlin.test.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test


class JUnit4StringTest {
  @Before fun setUp() {
  }
 
  @After fun tearDown() {
  }
 
  @Test fun testCapitalize() {
    var url = "http://localhost:3000/users/sign_in"
    val http = URL(url).openConnection() as HttpURLConnection
    http.requestMethod = "POST"
    http.doOutput = true

    val params = HashMap<String, String>()
    params.put("user[email]", "a@a.com")
    params.put("user[password]", "password")

    val joiner = StringJoiner("&")
    for ((key, value) in params) {
      joiner.add(URLEncoder.encode(key, "UTF-8")
              + "="
              + URLEncoder.encode(value, "UTF-8"))
    }

    val paramsBytes = joiner.toString().toByteArray()
    http.setFixedLengthStreamingMode(paramsBytes.size)
    http.setRequestProperty("Content-Type",
        "application/x-www-form-urlencoded; charset=UTF-8")
    http.connect()
    http.outputStream.use { outStream ->
      outStream.write(paramsBytes)
    }

    val body = http.inputStream.bufferedReader().use { it.readText() }

    assertEquals("Hello world!", body)
  }
}
