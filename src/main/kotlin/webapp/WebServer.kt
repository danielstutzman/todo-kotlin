package webapp

import app.App
import app.SignInFailure
import app.SignInSuccess
import app.SignUpFailure
import app.SignUpSuccess
import app.handleUsersSignInPost
import app.handleUsersSignUpPost
import com.google.gson.Gson
import db.Db
import spark.Request
import spark.Response
import spark.Service
import views.SignInForm
import views.SignUpErrors
import views.SignUpForm
import java.io.File
import java.security.SecureRandom
import java.sql.DriverManager
import java.util.Base64

const val COOKIE_NAME = "todo-kotlin"
const val CSRF_VALUE_LENGTH = 24

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
  val secureRandom = SecureRandom.getInstanceStrong()

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

  service.get("/users/sign_in") { req, res ->
    val csrfValue = generateCsrfValue(secureRandom)
    val form = SignInForm("", "")
    saveSession(loadSession(req).setCsrfValue(csrfValue), res)
    views.sign_in.template(req.pathInfo(), null, csrfValue, form).render().toString()
  }

  service.post("/users/sign_in") { req, res ->
    val session = loadSession(req)
    if (req.queryParams("authenticity_token") != session.csrfValue) {
      throw service.halt(401, "Expected ${session.csrfValue} but got ${req.queryParams("authenticity_token")}")
    }

    val newCsrfValue = generateCsrfValue(secureRandom)
    val form = SignInForm(
        req.queryParams("user[email]")!!,
        req.queryParams("user[password]")!!)
    val output = app.handleUsersSignInPost(form)
    when (output) {
      is SignInFailure ->
        views.sign_in.template(req.pathInfo(), output.alert, newCsrfValue, form).render().toString()
      is SignInSuccess ->
        res.redirect("/done")
    }
  }

  service.get("/users/sign_up") { req, res ->
    val csrfValue = generateCsrfValue(secureRandom)
    val form = SignUpForm("", "", "")
    saveSession(loadSession(req).setCsrfValue(csrfValue), res)
    views.sign_up.template(req.pathInfo(), null, csrfValue, form, SignUpErrors()).render().toString()
  }

  service.post("/users") { req, res ->
    val session = loadSession(req)
    if (req.queryParams("authenticity_token") != session.csrfValue) {
      throw service.halt(401, "Expected ${session.csrfValue} but got ${req.queryParams("authenticity_token")}")
    }

    val newCsrfValue = generateCsrfValue(secureRandom)
    val form = SignUpForm(
        req.queryParams("user[email]")!!,
        req.queryParams("user[password]")!!,
        req.queryParams("user[password_confirmation]")!!)
    val output = app.handleUsersSignUpPost(form)
    when (output) {
      is SignUpFailure ->
        views.sign_up.template(req.pathInfo(), null, newCsrfValue, form, output.errors).render().toString()
      is SignUpSuccess -> {
        val newSession = session.setUserId(output.setUserId)
        saveSession(newSession, res)
        res.redirect("/")
      }
    }
  }

  return service
}

data class Session(
    val userId: Int?,
    val csrfValue: String?
) {
  fun setUserId(newUserId: Int?) = Session(
      newUserId,
      this.csrfValue
  )

  fun setCsrfValue(newCsrfValue: String) = Session(
      this.userId,
      newCsrfValue
  )
}

fun loadSession(request: Request): Session {
  val cookieValue = request.cookie(COOKIE_NAME)
  if (cookieValue != null) {
    try {
      println("Got cookieValue: ${cookieValue}")
      val session = Gson().fromJson(cookieValue.replace('\'', '"'),
          Session::class.java)
      println("Got session ${session}")
      return session
    } catch (e: com.google.gson.JsonSyntaxException) {
      return Session(null, null)
    }
  } else {
    return Session(null, null)
  }
}

fun saveSession(session: Session, response: Response) {
  response.cookie(COOKIE_NAME,
      Gson().toJson(session).replace('\"', '\''))
}

fun generateCsrfValue(secureRandom: SecureRandom): String {
  val bytes = ByteArray(CSRF_VALUE_LENGTH * 6 / 8)
  secureRandom.nextBytes(bytes)
  return Base64.getEncoder().encodeToString(bytes)
}
