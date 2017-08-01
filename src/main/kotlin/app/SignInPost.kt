package app

import views.SignInForm
import webapp.ReqLog

sealed class UsersSignInPostOutput
data class SignInSuccess(val SetUserID: Int) : UsersSignInPostOutput()
data class SignInFailure(val alert: String) : UsersSignInPostOutput()

fun App.handleUsersSignInPost(form: SignInForm): UsersSignInPostOutput {
  ReqLog.start()
  try {
    var user = db.findUserByEmail(form.email)
    if (user == null) {
      return SignInFailure("Invalid email or password.")
    }

    if (this.passwordHasher.matches(form.password, user.encryptedPassword)) {
      return SignInFailure("Invalid email or password.")
    }

    return SignInSuccess(user.id)
  } finally {
    ReqLog.finish()
  }
}
