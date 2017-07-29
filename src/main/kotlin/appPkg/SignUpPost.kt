package appPkg

const val MIN_PASSWORD_LENGTH = 8
val EMAIL_ALREADY_TAKEN_ERROR = "has already been taken"
val PASSWORD_IS_TOO_SHORT_ERROR = "is too short (minimum is ${MIN_PASSWORD_LENGTH} characters)"
val PASSWORD_CONFIRMATION_DOESNT_MATCH_ERROR = "doesn't match Password"

sealed class UsersSignUpPostOutput
object PasswordIsTooShort : UsersSignUpPostOutput()
object PasswordConfirmationDoesntMatchError : UsersSignUpPostOutput()
object SignUpSuccess : UsersSignUpPostOutput()

fun App.handleUsersSignUpPost(form: SignUpForm): UsersSignUpPostOutput {
  if (form.passwordConfirmation != form.password) {
    return PasswordConfirmationDoesntMatchError
  }

  if (form.password.length < MIN_PASSWORD_LENGTH) {
    return PasswordIsTooShort
  }

  val encryptedPassword = "hash"

  db.createUser(form.email, encryptedPassword)

  return SignUpSuccess
}

public data class SignUpForm(
    val email: String,
    val password: String,
    val passwordConfirmation: String
) {}