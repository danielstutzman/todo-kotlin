import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput
import com.fizzed.rocker.runtime.OutputStreamOutput

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
