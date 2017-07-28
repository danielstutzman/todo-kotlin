import com.sun.net.httpserver.HttpServer
import org.jooq.SQLDialect
import org.jooq.generated.Tables.USERS
import org.jooq.impl.DSL
import java.io.File
import java.net.InetSocketAddress
import java.sql.DriverManager
import java.sql.Timestamp


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

    /*
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
    */

    System.setProperty("org.jooq.no-logo", "true")

    DSL.using(conn, SQLDialect.POSTGRES_9_5).let { create ->
      create.delete(USERS)
          .where(USERS.EMAIL.eq("a1"))
          .execute()

      val record = create.insertInto(USERS,
          USERS.EMAIL,
          USERS.ENCRYPTED_PASSWORD,
          USERS.CREATED_AT,
          USERS.UPDATED_AT)
          .values("a1",
              "b1",
              Timestamp(System.currentTimeMillis().toLong()),
              Timestamp(System.currentTimeMillis().toLong()))
          .returning(USERS.ID)
          .fetchOne()
      val newId = record.getValue(USERS.ID)
      println("Created record with ID ${newId}")

      create.select().from(USERS).fetch().let { rset ->
        for (row in rset) {
          val id = row.getValue(USERS.ID)
          val email = row.getValue(USERS.EMAIL)
          println("ID: $id EMAIL: $email")
        }
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
