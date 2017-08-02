package app

import views.FlashMessage
import views.SignInForm
import webapp.ReqLog

sealed class UsersSignInPostOutput
data class SignInSuccess(val SetUserID: Int) : UsersSignInPostOutput()
data class SignInFailure(val flash: FlashMessage) : UsersSignInPostOutput()

fun App.handleUsersSignInPost(form: SignInForm): UsersSignInPostOutput {
  ReqLog.start()
  try {
    var user = db.findUserByEmail(form.email)
    if (user == null) {
      return SignInFailure(FlashMessage("notice", "Invalid email or password."))
    }

    if (this.passwordHasher.matches(form.password, user.encryptedPassword)) {
      return SignInFailure(FlashMessage("notice", "Invalid email or password."))
    }

    if (user.confirmedAt == null) {
      return SignInFailure(FlashMessage("alert", "You have to confirm your account before continuing."))
    }


    return SignInSuccess(user.id)
  } finally {
    ReqLog.finish()
  }
}
