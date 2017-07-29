import appPkg.*
import dbPkg.Db
import spark.Service
import views.SignInForm
import views.SignUpErrors
import views.SignUpForm
import java.io.File
import java.sql.DriverManager

fun main(args: Array<String>) {
  val configJson = File("config/dev.json").readText()
  val config = parseConfigJson(configJson)

  System.setProperty("org.jooq.no-logo", "true")
  val creds = config.postgresCredentials
  val jdbcUrl = "jdbc:postgresql://${creds.hostname}:${creds.port}/${creds.databaseName}"
  val conn = DriverManager.getConnection(jdbcUrl, creds.username, creds.password)
  val db = Db(conn)

  val app = App(db, appPkg.SecurePasswordHasher(12))

  println("Starting server on 8080...")
  Service.ignite().port(8080).let { service ->
    service.initExceptionHandler { e ->
      e.printStackTrace()
    }

    if (true) { // if development mode
      service.staticFiles.location("/public")
      val projectDir = System.getProperty("user.dir")
      val staticDir = "/src/main/resources/public"
      service.staticFiles.externalLocation(projectDir + staticDir)
    }

    service.get("/") { req, res ->
      res.redirect("/users/sign_in")
    }

    service.get("/users/sign_in") { req, res ->
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

    service.get("/users/sign_up") { req, res ->
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
        is SignUpSuccess ->
          res.redirect("/")
      }
    }
  }
}

