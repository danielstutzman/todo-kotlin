import integration_test.getForCookiesAndAuthToken
import integration_test.post
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.net.URL

class IntegrationTest {
  companion object {
    @JvmStatic public fun main(args: Array<String>) {
      IntegrationTest().testSignInSuccess()
    }
  }

  @Before fun setUp() {
  }

  @After fun tearDown() {
  }


  @Test fun testSignInSuccess() {
    val (cookies, authToken) =
        getForCookiesAndAuthToken(URL("http://localhost:3000/users/sign_in"))
    val body = post(URL("http://localhost:3000/users/sign_in"), cookies, authToken)
    File("sign_in_success.html").writeText(body)
  }
}
