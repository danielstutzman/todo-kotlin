import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

fun main(args: Array<String>) {
  println("Starting server on 8000...")
  val server = HttpServer.create(InetSocketAddress(8000), 0);
  server.createContext("/", MyHandler())
  server.start()
}
