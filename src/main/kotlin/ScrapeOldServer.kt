import db.Db
import integration_test.getForCookiesAndAuthToken
import integration_test.post
import java.io.File
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
  scrapeFormPost("sign_up_success", "/users/sign_up", "/users", params + mapOf(
      "user[password]" to "password",
      "user[password_confirmation]" to "password"
  ))
  val savedUser = db.findUserByEmail(EMAIL1)!!
  scrapeFormPost("sign_up_exists", "/users/sign_up", "/users", params + mapOf(
      "user[password]" to "password",
      "user[password_confirmation]" to "password"
  ))
}

fun scrapeFormPost(scenarioName: String, getPath: String, postPath: String, params: Map<String, String>) {
  val (cookies, authToken) =
      getForCookiesAndAuthToken(URL(OLD_SERVER_URL + getPath))
  val body = post(URL(OLD_SERVER_URL + postPath), cookies, authToken)

  if (!File(SAVE_PATH).exists() && !File(SAVE_PATH).mkdirs()) {
    throw RuntimeException("Couldn't mkdirs ${SAVE_PATH}")
  }
  val outFile = File("${SAVE_PATH}/${scenarioName}.html")
  outFile.writeText(body)
  println(outFile)
}
