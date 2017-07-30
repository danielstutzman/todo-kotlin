package integration_test

import db.Db
import org.cyberneko.html.parsers.SAXParser
import org.xml.sax.InputSource
import webapp.Config
import webapp.PostgresCredentials
import webapp.startServer
import java.io.File
import java.io.FileWriter
import java.io.StringReader
import java.net.URL
import java.sql.DriverManager

const val NEW_SERVER_URL = "http://localhost:3001"

fun main(args: Array<String>) {
  System.setProperty("org.jooq.no-logo", "true")
  val creds = PostgresCredentials(
      "localhost",
      5432,
      "disable",
      "dan",
      "",
      "todo-kotlin")

  val jdbcUrl = "jdbc:postgresql://${creds.hostname}:${creds.port}/${creds.databaseName}"
  val conn = DriverManager.getConnection(jdbcUrl, creds.username, creds.password)
  val db = db.Db(conn)

  val service = startServer(Config(3001, creds))
  service.awaitInitialization()

  testSignInSuccess(db)

  service.stop()
}

fun testSignInSuccess(db: Db) {
  val params = mapOf(
      "utf8" to "\u2173",
      "user[email]" to EMAIL1,
      "commit" to "Sign up"
  )

  db.deleteUsers()
  val signUpSuccessBody = doFormPostNew("/users/sign_up", "/users", params + mapOf(
      "user[password]" to "password",
      "user[password_confirmation]" to "password"
  ))
  diffScenario("sign_up_success", signUpSuccessBody)
}

fun diffScenario(name: String, newBody: String) {
  val parser = SAXParser()

  val oldFile = "src/test/resources/scraped/${name}.html"
  val oldFileWriter = FileWriter(File("${name}.old.html"))
  parser.contentHandler = SAXWriteTagPerLine(oldFileWriter)
  parser.parse(InputSource(oldFile))
  oldFileWriter.close()

  val newFileWriter = FileWriter(File("${name}.new.html"))
  parser.contentHandler = SAXWriteTagPerLine(newFileWriter)
  parser.parse(InputSource(StringReader(newBody)))
  newFileWriter.close()

  File("${name}.html").writeText(newBody)
  ProcessBuilder(listOf(
      "/usr/bin/diff",
      "-u",
      "${name}.old.html",
      "${name}.new.html"))
      .redirectOutput(ProcessBuilder.Redirect.INHERIT)
      .redirectError(ProcessBuilder.Redirect.INHERIT)
      .start()
      .waitFor()

  File("${name}.old.html").delete()
  File("${name}.new.html").delete()
}

fun doFormPostNew(getPath: String, postPath: String, params: Map<String, String>): String {
//  TODO("Get cookies and authToken from getPath")
  val cookies = HashMap<String, String>()
  val authToken = "123"
  return post(URL(NEW_SERVER_URL + postPath), params, cookies, authToken)
}
