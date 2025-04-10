import org.junit.Test
import org.junit.Assert.*

class JSONNumberTests {

    @Test(expected = IllegalArgumentException::class)
    fun `should throw illegal argument exception with unsupported string format`() {
        // given
        val a = "123456789"

        // then
        JSONNumber(a)
    }

    @Test(expected = NullPointerException::class)
    fun `should throw illegal argument exception with unsupported null format`() {
        // given
        val a = null

        // then
        JSONNumber(a)
    }

    @Test
    fun `should successfully create JSONNumber objects`() {
        // given
        val a = 123
        val b = 123.14f
        val c = 123.12e7

        // when
        val number1 = JSONNumber(a)
        val number2 = JSONNumber(b)
        val number3 = JSONNumber(c)

        // then
        assertTrue(number1::class.java == JSONNumber::class.java)
        assertTrue(number2::class.java == JSONNumber::class.java)
        assertTrue(number3::class.java == JSONNumber::class.java)
    }

    @Test
    fun `should test correct stringify format`() {
        // given
        val a = JSONNumber(123)
        val b = JSONNumber(-123.14f)
        val c = JSONNumber(123.12e7)

        // then
        assertEquals("123", a.stringify())
        assertEquals("-123.14", b.stringify())
        assertEquals("1.2312E9", c.stringify())
    }
}