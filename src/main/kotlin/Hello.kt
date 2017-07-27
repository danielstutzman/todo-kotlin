import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.sql.DriverManager


fun main(args: Array<String>) {
  val jdbcUrl = "jdbc:postgresql://localhost:5432/todo-go"
  Class.forName("org.postgresql.Driver")
  val conn = DriverManager.getConnection(jdbcUrl, "dan", "")
  val stmt = conn.createStatement()
  val rset = stmt.executeQuery("SELECT 1")
  while (rset.next()) {
    println(rset.getString(1))
  }
  rset.close()
  stmt.close()
  conn.close()

  println("Starting server on 8000...")
  val server = HttpServer.create(InetSocketAddress(8000), 0);
  server.createContext("/", MyHandler())
  server.start()
}
