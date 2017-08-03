package integration_test

import db.Db
import db.User
import db.UserCreated
import java.net.URL

fun runScenarios(
    urlPrefix: String,
    db: Db,
    handleResult: (scenarioName: String, htmlBody: String) -> Unit
) {
  val savedUser = runSignUpScenarios(urlPrefix, db, handleResult)
  runSignInScenarios(urlPrefix, db, savedUser, handleResult)
}

/** @return savedUser */
fun runSignUpScenarios(
    urlPrefix: String,
    db: Db,
    handleResult: (scenarioName: String, htmlBody: String) -> Unit
): User {
  val params = mapOf(
      "utf8" to "\u2173",
      "user[email]" to EMAIL1,
      "commit" to "Sign up"
  )

  // Scenario: sign_up_needs_confirm
  db.deleteUsers()
  handleResult("sign_up_needs_confirm", doFormPost(urlPrefix,
      "/users/sign_up", "/users", params + mapOf(
      "user[password]" to "password",
      "user[password_confirmation]" to "password"
  )))
  val savedUser = db.findUserByEmail(EMAIL1)!!

  // Scenario: sign_up_exists
  handleResult("sign_up_exists", doFormPost(urlPrefix,
      "/users/sign_up", "/users", params + mapOf(
      "user[password]" to "password",
      "user[password_confirmation]" to "password"
  )))

  // Scenario: sign_up_mismatch
  db.deleteUsers()
  handleResult("sign_up_mismatch", doFormPost(urlPrefix,
      "/users/sign_up", "/users", params + mapOf(
      "user[password]" to "password",
      "user[password_confirmation]" to "different"
  )))

  // Scenario: sign_up_short
  db.deleteUsers()
  handleResult("sign_up_short", doFormPost(urlPrefix,
      "/users/sign_up", "/users", params + mapOf(
      "user[password]" to "short",
      "user[password_confirmation]" to "short"
  )))

  return savedUser
}

fun runSignInScenarios(
    urlPrefix: String,
    db: Db,
    savedUser: User,
    handleResult: (scenarioName: String, htmlBody: String) -> Unit
) {
  val params = mapOf(
      "utf8" to "\u2173",
      "user[email]" to EMAIL1,
      "user[password]" to "password",
      "commit" to "Sign in"
  )

  // Scenario: sign_in_needs_confirm
  db.deleteUsers()
  db.createUser(savedUser.email, savedUser.encryptedPassword) as UserCreated
  handleResult("sign_in_needs_confirm", doFormPost(urlPrefix,
      "/users/sign_in", "/users/sign_in", params))  // Scenario: sign_in_needs_confirm

  db.deleteUsers()
  val userCreated = db.createUser(savedUser.email, savedUser.encryptedPassword) as UserCreated
  db.confirmUser(userCreated.user.id, db.now())
  handleResult("sign_in_successful", doFormPost(urlPrefix,
      "/users/sign_in", "/users/sign_in", params))
}

fun doFormPost(urlPrefix: String, getPath: String, postPath: String, params: Map<String, String>): String {
  val (cookies, authToken) =
      getForCookiesAndAuthToken(URL(urlPrefix + getPath))
  val body = post(URL(urlPrefix + postPath), params, cookies, authToken)
  return body
}