import appPkg.*
import dbPkg.Db
import spark.Service
import views.SignInForm
import java.io.File
import java.sql.DriverManager


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

    /*
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
    */

    conn.close()
  }
}

fun main(args: Array<String>) {
  val configJson = File("config/dev.json").readText()
  val config = parseConfigJson(configJson)

//  testDatabase(config.postgresCredentials)

  System.setProperty("org.jooq.no-logo", "true")
  val creds = config.postgresCredentials
  val jdbcUrl = "jdbc:postgresql://${creds.hostname}:${creds.port}/${creds.databaseName}"
  val conn = DriverManager.getConnection(jdbcUrl, creds.username, creds.password)
  val db = Db(conn)

  val app = App(db)

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
          req.queryParams("user[email]"),
          req.queryParams("user[password]"))
      val output = app.handleUsersSignInPost(form)
      when (output) {
        is SignInSuccess -> {
          res.redirect("/done")
        }
        is SignInFailure -> {
          views.sign_in.template(req.pathInfo(), output.alert, form).render().toString()
        }
      }
    }

    service.post("/users/sign_up") { req, res ->
      val form = SignUpForm(
          req.params("user[email]"),
          req.params("user[password]"),
          req.params("user[passwordConfirmation]"))
      val output = app.handleUsersSignUpPost(form)
      if (output == SignUpSuccess) {
        res.redirect("/done")
      } else {
        res.redirect("/done")
      }
    }
  }
}

