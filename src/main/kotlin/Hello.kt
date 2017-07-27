import com.sun.net.httpserver.HttpServer
import java.io.File
import java.net.InetSocketAddress
import java.sql.DriverManager

fun testDatabase(creds: PostgresCredentials) {
  val jdbcUrl = "jdbc:postgresql://${creds.hostname}:${creds.port}/${creds.databaseName}"
  val conn = DriverManager.getConnection(jdbcUrl, creds.username, creds.password)
  val stmt = conn.createStatement()
  val rset = stmt.executeQuery("SELECT 1")
  while (rset.next()) {
    println(rset.getString(1))
  }
  rset.close()
  stmt.close()
  conn.close()
}

fun main(args: Array<String>) {
  val configJson = File("config/dev.json").readText()
  val config = parseConfigJson(configJson)

  testDatabase(config.postgresCredentials)

  println("Starting server on 8000...")
  val server = HttpServer.create(InetSocketAddress(8000), 0);
  server.createContext("/", MyHandler())
  server.start()
}
