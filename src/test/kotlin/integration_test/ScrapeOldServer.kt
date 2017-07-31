package integration_test

import db.Db
import org.cyberneko.html.parsers.SAXParser
import org.xml.sax.InputSource
import webapp.PostgresCredentials
import java.io.File
import java.io.FileWriter
import java.io.StringReader
import java.net.URL
import java.sql.DriverManager

const val EMAIL1 = "a@a.com"
const val OLD_SERVER_URL = "http://localhost:3000"
const val SAVE_PATH = "src/test/resources/scraped"

fun main(args: Array<String>) {
  System.setProperty("org.jooq.no-logo", "true")

  val creds = PostgresCredentials(
      "localhost",
      5432,
      "disabled",
      "dan",
      "",
      "todo_rails_development"
  )
  val jdbcUrl = "jdbc:postgresql://${creds.hostname}:${creds.port}/${creds.databaseName}"
  val conn = DriverManager.getConnection(jdbcUrl, creds.username, creds.password)
  val db = Db(conn)

  val params = mapOf(
      "utf8" to "\u2173",
      "user[email]" to EMAIL1,
      "commit" to "Sign up"
  )

  db.deleteUsers()
  saveScenario("sign_up_needs_confirm",
      doFormPost("/users/sign_up", "/users", params + mapOf(
          "user[password]" to "password",
          "user[password_confirmation]" to "password"
      )))
  val savedUser = db.findUserByEmail(EMAIL1)!!

  saveScenario("sign_up_exists",
      doFormPost("/users/sign_up", "/users", params + mapOf(
          "user[password]" to "password",
          "user[password_confirmation]" to "password"
      )))
}

fun saveScenario(name: String, body: String) {
  if (!File(SAVE_PATH).exists() && !File(SAVE_PATH).mkdirs()) {
    throw RuntimeException("Couldn't mkdirs ${SAVE_PATH}")
  }
  val outFile = File("${SAVE_PATH}/${name}.html")
  FileWriter(outFile).use { fileWriter ->
    val parser = SAXParser()
    parser.contentHandler = SAXWriteTagPerLine(fileWriter)
    parser.parse(InputSource(StringReader(body)))
  }


  println(outFile)
}


fun doFormPost(getPath: String, postPath: String, params: Map<String, String>): String {
  val (cookies, authToken) =
      getForCookiesAndAuthToken(URL(OLD_SERVER_URL + getPath))
  val body = post(URL(OLD_SERVER_URL + postPath), params, cookies, authToken)
  return body
}
