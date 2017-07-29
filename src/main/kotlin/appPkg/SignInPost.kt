package appPkg

import views.SignInForm

sealed class UsersSignInPostOutput
data class SignInSuccess(val SetUserID: Int) : UsersSignInPostOutput()
data class SignInFailure(val alert: String) : UsersSignInPostOutput()

fun App.handleUsersSignInPost(form: SignInForm): UsersSignInPostOutput {
  var user = db.findUserByEmail(form.email)
  if (user == null) {
    return SignInFailure("wrong email")
  }

  if (form.password != user.encryptedPassword) {
    return SignInFailure("wrong password")
  }

  return SignInSuccess(user.id)
}
