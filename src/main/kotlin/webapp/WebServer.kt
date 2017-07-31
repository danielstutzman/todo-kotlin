package webapp

import app.App
import app.SignInFailure
import app.SignInSuccess
import app.SignUpFailure
import app.SignUpSuccess
import app.handleUsersSignInPost
import app.handleUsersSignUpPost
import db.Db
import spark.Request
import spark.Response
import spark.Service
import views.SignInForm
import views.SignUpErrors
import views.SignUpForm
import java.io.File
import java.sql.DriverManager

const val COOKIE_NAME = "todo-kotlin"

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

  val app = App(db,
      app.SecurePasswordHasher(12),
      app.SecureTokenGenerator(16))

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

  service.get("/") { _, res ->
    res.redirect("/users/sign_in")
  }

  service.get("/users/sign_in") { req, _ ->
    val form = SignInForm("", "")
    views.sign_in.template(req.pathInfo(), null, form).render().toString()
  }

  service.post("/users/sign_in") { req, res ->
    val form = SignInForm(
        req.queryParams("user[email]")!!,
        req.queryParams("user[password]")!!)
    val output = app.handleUsersSignInPost(form)
    when (output) {
      is SignInFailure ->
        views.sign_in.template(req.pathInfo(), output.alert, form).render().toString()
      is SignInSuccess ->
        res.redirect("/done")
    }
  }

  service.get("/users/sign_up") { req, _ ->
    val form = SignUpForm("", "", "")
    views.sign_up.template(req.pathInfo(), null, form, SignUpErrors()).render().toString()
  }

  service.post("/users") { req, res ->
    val form = SignUpForm(
        req.queryParams("user[email]")!!,
        req.queryParams("user[password]")!!,
        req.queryParams("user[password_confirmation]")!!)
    val output = app.handleUsersSignUpPost(form)
    when (output) {
      is SignUpFailure ->
        views.sign_up.template(req.pathInfo(), null, form, output.errors).render().toString()
      is SignUpSuccess -> {
        saveSession(Session(output.setUserId), res)
        res.redirect("/")
      }
    }
  }

  return service
}

data class Session(val userId: Int?) {}

fun loadSession(request: Request): Session {
  val userIdString = request.cookie(COOKIE_NAME)
  val userId = userIdString?.toInt()
  return Session(userId)
}

fun saveSession(session: Session, response: Response) {
  response.cookie(COOKIE_NAME, session.userId.toString())
}
