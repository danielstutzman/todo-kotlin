package integration_test

import db.Db
import webapp.Config
import webapp.PostgresCredentials
import webapp.startServer
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

  startServer(Config(3001, creds))
  testSignInSuccess(db)
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
  println(signUpSuccessBody)
}

fun doFormPostNew(getPath: String, postPath: String, params: Map<String, String>): String {
  val (cookies, authToken) =
      getForCookiesAndAuthToken(URL(NEW_SERVER_URL + getPath))
  val body = post(URL(NEW_SERVER_URL + postPath), params, cookies, authToken)
  return body
}
