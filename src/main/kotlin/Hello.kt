import com.sun.net.httpserver.HttpServer
import java.io.File
import java.net.InetSocketAddress
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement


fun testDatabase(creds: PostgresCredentials) {
  val jdbcUrl = "jdbc:postgresql://${creds.hostname}:${creds.port}/${creds.databaseName}"
  DriverManager.getConnection(jdbcUrl, creds.username, creds.password).let { conn ->
    /*
    conn.createStatement().let { stmt ->
      stmt.executeQuery("SELECT 1").let { rset ->
        while (rset.next()) {
          println(rset.getString(1))
        }
        rset.close()
      }
      stmt.close()
    }
    */
    val sql = "INSERT INTO users (a) VALUES (?)"
    conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).let { stmt ->
      stmt.setInt(1, 5)
      stmt.executeUpdate().let { numRows ->
        if (numRows != 1) {
          throw RuntimeException("executeUpdate returned ${numRows}")
        }
      }
      stmt.getGeneratedKeys().let { rset: ResultSet ->
        if (!rset.next()) {
          throw RuntimeException("Can't find generated ID of insert")
        }
        val key = rset.getInt(1)
        println("PK is ${key}")
        rset.close()
      }
    }
    conn.close()
  }
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
