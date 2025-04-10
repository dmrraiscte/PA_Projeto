import org.junit.Test
import org.junit.Assert.*

class JSONObjectTests {
    
    @Test
    fun `should test json object creation with no fields`() {
        // given
        val a: Map<String, JSONValue<*>> = mapOf()
        
        // when
        val b = JSONObject(a)
        
        // then
        assertEquals("{ }", b.stringify())
    }

    @Test
    fun `should test json object creation with various fields`() {
        // given
        val a = mapOf("shift" to JSONValue("night"))
        val b = JSONValue(JSONObject(mapOf("name" to JSONValue("Pedro"))))
        val c = mapOf("shift" to JSONValue("night"), "price" to JSONValue(JSONNumber(2.34e7)), "child" to b, "possibilities" to JSONValue(JSONArray(listOf(JSONValue(JSONNumber(234)), JSONValue(JSONNumber(237))))))

        // when
        val obj1 = JSONValue(JSONObject(a))
        val obj2 = JSONValue(JSONObject(c))

        // then
        assertEquals("{\n    \"shift\" : \"night\"\n}", obj1.stringify())
        assertEquals("{\n    \"shift\" : \"night\",\n    \"price\" : 2.34E7,\n    \"child\" : {\n        \"name\" : \"Pedro\"\n    },\n    \"possibilities\" : [ 234, 237 ]\n}", obj2.stringify())
    }


}