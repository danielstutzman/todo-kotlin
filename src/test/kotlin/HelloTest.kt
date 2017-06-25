import org.junit.After
import org.junit.Before
import org.junit.Test
import junit.framework.TestCase
import kotlin.test.*
 
class JUnit4StringTest {
  @Before fun setUp() {
  }
 
  @After fun tearDown() {
  }
 
  @Test fun testCapitalize() {
    assertEquals("Hello world!", "hello world!".capitalize())
  }
}
