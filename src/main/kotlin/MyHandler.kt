import com.fizzed.rocker.runtime.OutputStreamOutput
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import java.io.File
import java.net.HttpURLConnection


class MyHandler : HttpHandler {
  override fun handle(exchange: HttpExchange) {
    try {
      val path = exchange.requestURI.path
      if (path.startsWith("/assets")) {
        val resource = javaClass.getResource("/public${path}")
        if (resource == null) {
          sendNotFound(exchange)
        } else {
          exchange.responseBody.use { body ->
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0)
            body.write(File(resource.file).readBytes())
          }
        }
      } else if (path == "/") {
        if (exchange.requestMethod == "GET") {
          handleRootGet(exchange)
        } else {
          sendBadMethod(exchange)
        }
      } else if (path == "/users/sign_in") {
        if (exchange.requestMethod == "GET") {
          handleUsersSignInGet(exchange)
        } else {
          sendBadMethod(exchange)
        }
      } else {
        sendNotFound(exchange)
      }
    } catch (e: Throwable) {
      e.printStackTrace()
      sendInternalError(exchange)
    }
  }

  private fun sendInternalError(exchange: HttpExchange) {
    exchange.responseBody.use { body ->
      exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0)
      body.write("Internal Error".toByteArray())
    }
  }

  private fun sendNotFound(exchange: HttpExchange) {
    exchange.responseBody.use { body ->
      exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0)
      body.write("Not found".toByteArray())
    }
  }

  private fun sendBadMethod(exchange: HttpExchange) {
    exchange.responseBody.use { body ->
      exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0)
      body.write("Bad method".toByteArray())
    }
  }

  private fun handleRootGet(exchange: HttpExchange) {
    exchange.responseBody.use { body ->
      exchange.responseHeaders["Location"] = "/users/sign_in"
      exchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_TEMP, 0)
    }
  }

  private fun handleUsersSignInGet(exchange: HttpExchange) {
    exchange.responseBody.use { body ->
      exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0)
      views.sign_in
          .template("World")
          .render({ contentType, charsetName ->
            OutputStreamOutput(contentType, body, charsetName)
          })
    }
  }
}
