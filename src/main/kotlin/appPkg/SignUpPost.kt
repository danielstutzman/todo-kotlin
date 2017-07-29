package appPkg

import dbPkg.EmailAlreadyTaken
import dbPkg.UserCreated
import views.SignUpErrors
import views.SignUpForm

const val MIN_PASSWORD_LENGTH = 8
val EMAIL_ALREADY_TAKEN_ERROR = "has already been taken"

sealed class UsersSignUpPostOutput
data class SignUpFailure(val errors: SignUpErrors) : UsersSignUpPostOutput()
object SignUpSuccess : UsersSignUpPostOutput()

fun App.handleUsersSignUpPost(form: SignUpForm): UsersSignUpPostOutput {
  if (form.passwordConfirmation != form.password) {
    return SignUpFailure(SignUpErrors().setPasswordConfirmation(
        "doesn't match Password"))
  }

  if (form.password.length < MIN_PASSWORD_LENGTH) {
    return SignUpFailure(SignUpErrors().setPassword(
        "is too short (minimum is ${MIN_PASSWORD_LENGTH} characters)"))
  }

  val encryptedPassword = "hash"

  var result = db.createUser(form.email, encryptedPassword)
  return when (result) {
    is EmailAlreadyTaken ->
      return SignUpFailure(SignUpErrors().setEmail(
          "has already been taken"))
    is UserCreated ->
      SignUpSuccess
  }
}
