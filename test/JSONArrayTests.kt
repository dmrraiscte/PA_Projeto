import org.junit.Test
import org.junit.Assert.*

class JSONArrayTests {

    @Test
    fun `should test json array creation with empty array`() {
        // given
        val arr = listOf<JSONValue<String>>()

        // when
        val a = JSONArray(arr)

        // then
        assertEquals( "[  ]" , a.stringify())

    }

    @Test
    fun `should test json array with various types`() {
        // given
        val arr1 = listOf(JSONValue(JSONNumber(1)), JSONValue(JSONNumber(-2.34e7)))
        val arr2 = listOf(JSONValue("este elemento é uma string"), JSONValue(JSONNumber(-2.34e7)))
        val arr3 = listOf(JSONValue("este elemento é uma string"), JSONValue(JSONNumber(-2.34e7)), JSONObject(mapOf("teste" to JSONValue(true))))

        // when
        val a1 = JSONArray(arr1)
        val a2 = JSONArray(arr2)
        val a3 = JSONArray(arr3)

        // then
        assertEquals( "[ 1, -2.34E7 ]" , a1.stringify())
        assertEquals( "[ \"este elemento é uma string\", -2.34E7 ]" , a2.stringify())
        assertEquals( "[ \"este elemento é uma string\", -2.34E7, {\n    \"teste\" : true\n} ]" , a3.stringify())
    }
}