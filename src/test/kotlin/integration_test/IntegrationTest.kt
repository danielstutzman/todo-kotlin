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

  testSignUpNeedsConfirm(db)

  service.stop()
}

fun testSignUpNeedsConfirm(db: Db) {
  val params = mapOf(
      "utf8" to "\u2173",
      "user[email]" to EMAIL1,
      "commit" to "Sign up"
  )

  db.deleteUsers()
  val body = doFormPostNew("/users/sign_up", "/users", params + mapOf(
      "user[password]" to "password",
      "user[password_confirmation]" to "password"
  ))
  diffScenario("sign_up_needs_confirm", body)
}

fun diffScenario(name: String, newBody: String) {
  val parser = SAXParser()

  val newFile = File("${name}.html")
  FileWriter(newFile).use { fileWriter ->
    parser.contentHandler = SAXWriteTagPerLine(fileWriter)
    parser.parse(InputSource(StringReader(newBody)))
  }

  val commandArgs = listOf(
      "/usr/bin/diff",
      "-u",
      "src/test/resources/scraped/${name}.html",
      newFile.path)
  ProcessBuilder(commandArgs)
      .redirectOutput(ProcessBuilder.Redirect.INHERIT)
      .redirectError(ProcessBuilder.Redirect.INHERIT)
      .start()
      .waitFor()

  newFile.delete()
}

fun doFormPostNew(getPath: String, postPath: String, params: Map<String, String>): String {
  val (cookies, authToken) =
      getForCookiesAndAuthToken(URL(NEW_SERVER_URL + getPath))
  return post(URL(NEW_SERVER_URL + postPath), params, cookies, authToken)
}
