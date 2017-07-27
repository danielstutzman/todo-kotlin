import com.fizzed.rocker.runtime.OutputStreamOutput
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler

class MyHandler : HttpHandler {
  override fun handle(exchange: HttpExchange) {
    exchange.sendResponseHeaders(200, 0)

    views.index
        .template("World")
        .render({ contentType, charsetName ->
          OutputStreamOutput(contentType, exchange.getResponseBody(), charsetName)
        })

    exchange.getResponseBody().close()
  }
}
