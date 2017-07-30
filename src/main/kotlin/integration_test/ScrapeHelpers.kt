package integration_test

import org.cyberneko.html.parsers.SAXParser
import org.xml.sax.InputSource
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.HashMap
import java.util.StringJoiner

fun applyCookies(http: HttpURLConnection, cookies: Map<String, String>) {
  for ((key, value) in cookies) {
    http.addRequestProperty("Cookie", "${key}=${value}")
  }
}

fun followRedirect(url: URL, cookies: Map<String, String>): String {
  val http = url.openConnection() as HttpURLConnection
  applyCookies(http, cookies)
  http.connect()
  val body = http.inputStream.bufferedReader().use { it.readText() }
  return body
}

fun extractCookiesFromSetCookieHeader(http: HttpURLConnection): Map<String, String> {
  val cookies = HashMap<String, String>()
  val cookiesHeader = http.headerFields.get("Set-Cookie")
  if (cookiesHeader == null) {
    throw NullPointerException("Can't find Set-Cookie header")
  }
  for (cookieString in cookiesHeader) {
    val keyEqualsValue = cookieString.split(";")[0]
    val parts = keyEqualsValue.split("=")
    cookies.put(parts[0], parts[1])
  }
  return cookies
}

fun urlEncodeParams(params: Map<String, String>): String {
  val joiner = StringJoiner("&")
  for ((key, value) in params) {
    joiner.add(URLEncoder.encode(key, "UTF-8")
        + "="
        + URLEncoder.encode(value, "UTF-8"))
  }
  return joiner.toString()
}

fun getForCookiesAndAuthToken(url: URL): Pair<Map<String, String>, String> {
  val http = url.openConnection() as HttpURLConnection

  try {
    http.connect()
  } catch (e: java.net.ConnectException) {
    throw RuntimeException("Couldn't connect to ${url}", e)
  }

  if (http.responseCode != HttpURLConnection.HTTP_OK) {
    throw RuntimeException("Got ${http.responseCode} from ${url}")
  }

  val cookies = extractCookiesFromSetCookieHeader(http)

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
fun post(url: URL, cookies: Map<String, String>, authToken: String): String {
  val http = url.openConnection() as HttpURLConnection
  http.requestMethod = "POST"
  http.doOutput = true
  applyCookies(http, cookies)
  http.setRequestProperty("Content-Type",
      "application/x-www-form-urlencoded; charset=UTF-8")
  http.instanceFollowRedirects = false
  http.connect()

  http.outputStream.use { outStream ->
    val params = HashMap<String, String>()
    params.put("user[email]", "a@a.com")
    params.put("user[password]", "password")
    params.put("authenticity_token", authToken)
    outStream.write(urlEncodeParams(params).toByteArray())
  }

  // Have to manually follow redirect; otherwise cookies don't get resent
  if (http.responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
      http.responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
      http.responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
    val newUrl = URL(http.getHeaderField("Location"))
    val newCookies = cookies + extractCookiesFromSetCookieHeader(http)
    return followRedirect(newUrl, newCookies)
  } else {
    val body = http.inputStream.bufferedReader().use { it.readText() }
    return body
  }
}

