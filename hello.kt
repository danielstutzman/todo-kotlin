import com.sun.net.httpserver.HttpServer
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import java.net.InetSocketAddress
import com.fizzed.rocker.runtime.OutputStreamOutput
import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput

data class User(val name: String, val id: Int)

sealed class Expr
data class Const(val number: Double) : Expr()
data class Sum(val e1: Expr, val e2: Expr) : Expr()
object NotANumber : Expr()

fun getUser(): User {
  return User("Alex", 1)
}

fun eval(expr: Expr): Double = when(expr) {
	is Const -> expr.number
	is Sum -> eval(expr.e1) + eval(expr.e2)
	NotANumber -> Double.NaN
}


fun main(args: Array<String>) {
  val user = getUser()
  println("name = ${user.name}, id = ${user.id}")

  val (name, id) = getUser()
  println("name = $name, id = $id")

  println("name = ${getUser().component1()}, id = ${getUser().component2()}")


  val expr:Expr = Sum(Const(5.0), Const(8.0))
  println("${expr} -> ${eval(expr)}")

  println("Starting server on 8000...")
  val server = HttpServer.create(InetSocketAddress(8000), 0);
  server.createContext("/", MyHandler())
  server.start()
}

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
