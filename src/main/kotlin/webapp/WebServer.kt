package webapp

import app.App
import app.FakePasswordHasher
import app.SecurePasswordHasher
import db.Db
import spark.Service
import java.io.File
import java.sql.DriverManager

fun main(args: Array<String>) {
  val configJson = File("config/dev.json").readText()
  val config = parseConfigJson(configJson)
  startServer(config)
}

fun startServer(config: Config): Service {
  System.setProperty("org.jooq.no-logo", "true")
  val creds = config.postgresCredentials
  val jdbcUrl = "jdbc:postgresql://${creds.hostname}:${creds.port}/${creds.databaseName}"
  val conn = DriverManager.getConnection(jdbcUrl, creds.username, creds.password)
  val db = Db(conn)
  val passwordHasher =
      if (config.hashPasswords) SecurePasswordHasher(31)
      else FakePasswordHasher()

  val app = App(db,
      passwordHasher,
      app.SecureTokenGenerator(16))
  val webapp = Webapp(app, SessionStorage("secret"))

  println("Starting server on ${config.port}...")
  val service = Service.ignite().port(config.port)

  service.initExceptionHandler { e ->
    e.printStackTrace()
  }

  if (true) { // if development mode
    service.staticFiles.location("/public")
    val projectDir = System.getProperty("user.dir")
    val staticDir = "/src/main/resources/public"
    service.staticFiles.externalLocation(projectDir + staticDir)
  }

  service.get("/", webapp.root)
  service.get("/users/sign_in", webapp.usersSignInGet)
  service.post("/users/sign_in", webapp.usersSignInPost)
  service.get("/users/sign_up", webapp.usersSignUpGet)
  service.post("/users", webapp.usersSignUpPost)

  return service
}

