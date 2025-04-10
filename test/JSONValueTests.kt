import org.junit.Test
import org.junit.Assert.*

class JSONValueTests {

    @Test(expected = IllegalArgumentException::class)
    fun `should throw illegal argument exception with unsupported type`() {
        // given
        val a = listOf(1,2,6,1)

        // then
        JSONValue(a)
    }

    @Test
    fun `should successfully create JSONValue objects`() {
        // given
        val a = "abc"
        val b = JSONNumber(123)
        val c = JSONNumber(1.65e5)
        val d = true
        val e = null
        // todo - inserir os restantes tipos de jsonarray e jsonobject

        // when
        val value1 = JSONValue(a)
        val value2 = JSONValue(b)
        val value3 = JSONValue(c)
        val value4 = JSONValue(d)
        val value5 = JSONValue(e)

        // then
        assertTrue(value1::class.java == JSONValue::class.java)
        assertTrue(value2::class.java == JSONValue::class.java)
        assertTrue(value3::class.java == JSONValue::class.java)
        assertTrue(value4::class.java == JSONValue::class.java)
        assertTrue(value5::class.java == JSONValue::class.java)
    }

    @Test
    fun `should test correct stringify format for every type`() {
        // given
        val a = JSONValue("abc")
        val b = JSONValue(JSONNumber(123e45))
        val c = JSONValue(null)
        val d = JSONValue(true)
        // todo - inserir restantes tipos

        // then
        assertEquals("\"abc\"", a.stringify())
        assertEquals("1.23E47", b.stringify())
        assertEquals("null", c.stringify())
        assertEquals("true", d.stringify())
    }

}