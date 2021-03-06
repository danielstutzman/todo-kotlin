package webapp

import app.App
import app.SignInFailure
import app.SignInSuccess
import app.SignUpFailure
import app.SignUpSuccess
import app.handleUsersSignInPost
import app.handleUsersSignUpPost
import spark.Request
import spark.Response
import views.FlashMessage
import views.SignInForm
import views.SignUpErrors
import views.SignUpForm

val SIGNED_UP_BUT_UNCONFIRMED = FlashMessage(
    "notice",
    "A message with a confirmation link has been sent to your email address. Please open the link to activate your account."
)

data class Webapp(
    val app: App,
    val sessionStorage: SessionStorage
) {
  val root = { _: Request, res: Response ->
    res.redirect("/users/sign_in")
  }

  val usersSignInGet = { req: Request, res: Response ->
    ReqLog.start(req)
    try {
      val oldSession = sessionStorage.loadSessionAndCheckCsrf(req)
      val form = SignInForm("", "")

      val newSession = sessionStorage.updateSession(oldSession, res)
      views.sign_in.template(
          req.pathInfo(),
          newSession.flash,
          newSession.csrfValue,
          form
      ).render().toString()
    } finally {
      ReqLog.finish()
    }
  }

  val usersSignInPost = { req: Request, res: Response ->
    ReqLog.start(req)
    try {
      val oldSession = sessionStorage.loadSessionAndCheckCsrf(req)
      val form = SignInForm(
          req.queryParams("user[email]")!!,
          req.queryParams("user[password]")!!)

      val output = app.handleUsersSignInPost(form)

      val newSession = sessionStorage.updateSession(oldSession, res)
      when (output) {
        is SignInFailure ->
          views.sign_in.template(
              req.pathInfo(),
              output.flash,
              newSession.csrfValue,
              SignInForm("", "")
          ).render().toString()
        is SignInSuccess ->
          res.redirect("/done")
      }
    } finally {
      ReqLog.finish()
    }
  }

  val usersSignUpGet = { req: Request, res: Response ->
    ReqLog.start(req)
    try {
      val oldSession = sessionStorage.loadSessionAndCheckCsrf(req)
      val form = SignUpForm("", "", "")

      val newSession = sessionStorage.updateSession(oldSession, res)
      views.sign_up.template(
          req.pathInfo(),
          null,
          newSession.csrfValue,
          form,
          SignUpErrors()
      ).render().toString()
    } finally {
      ReqLog.finish()
    }
  }

  val usersSignUpPost = { req: Request, res: Response ->
    ReqLog.start(req)
    try {
      val oldSession = sessionStorage.loadSessionAndCheckCsrf(req)
      val form = SignUpForm(
          req.queryParams("user[email]")!!,
          req.queryParams("user[password]")!!,
          req.queryParams("user[password_confirmation]")!!)

      val output = app.handleUsersSignUpPost(form)

      when (output) {
        is SignUpFailure ->
          views.sign_up.template(
              req.pathInfo(),
              null,
              oldSession.csrfValue,
              form,
              output.errors
          ).render().toString()
        is SignUpSuccess -> {
          val newSession = oldSession.copy(
              userId = output.setUserId,
              flash = SIGNED_UP_BUT_UNCONFIRMED)
          sessionStorage.updateSession(newSession, res)
          res.redirect("/")
        }
      }
    } finally {
      ReqLog.finish()
    }
  }
}
