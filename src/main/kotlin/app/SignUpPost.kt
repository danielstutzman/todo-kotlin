package app

import db.EmailAlreadyTaken
import db.UserCreated
import views.SignUpErrors
import views.SignUpForm
import webapp.ReqLog

const val MIN_PASSWORD_LENGTH = 8
val EMAIL_ALREADY_TAKEN_ERROR = "has already been taken"

sealed class UsersSignUpPostOutput
data class SignUpFailure(val errors: SignUpErrors) : UsersSignUpPostOutput()
data class SignUpSuccess(val setUserId: Int) : UsersSignUpPostOutput()

fun App.handleUsersSignUpPost(reqLog: ReqLog, form: SignUpForm): UsersSignUpPostOutput {
  if (form.passwordConfirmation != form.password) {
    return SignUpFailure(SignUpErrors().setPasswordConfirmation(
        "doesn't match Password"))
  }

  if (form.password.length < MIN_PASSWORD_LENGTH) {
    return SignUpFailure(SignUpErrors().setPassword(
        "is too short (minimum is ${MIN_PASSWORD_LENGTH} characters)"))
  }

  val encryptedPassword = reqLog.log("passwordHasher.hash") {
    passwordHasher.hash(form.password)
  }

  var result = reqLog.log("db.createUser") {
    db.createUser(form.email, encryptedPassword)
  }

  return when (result) {
    is EmailAlreadyTaken ->
      return SignUpFailure(SignUpErrors().setEmail(
          "has already been taken"))
    is UserCreated ->
      SignUpSuccess(result.user.id)
  }
}
