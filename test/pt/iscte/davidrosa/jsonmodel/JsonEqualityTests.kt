package pt.iscte.davidrosa.jsonmodel

import org.junit.Test
import org.junit.Assert.*

class JsonEqualityTests {

    @Test
    fun `should test equality for all primitive types of json data`() {
        // given
        val string1 = JsonString("test")
        val string2 = Json.of("test")

        val number1 = JsonNumber(123)
        val number2 = Json.of(123)

        val bool1 = JsonBoolean(true)
        val bool2 = Json.of(true)

        val nul1 = JsonNull
        val nul2 = Json.of(null)


        // then
        assertEquals(string1, string2)
        assertEquals(number1, number2)
        assertEquals(bool1, bool2)
        assertEquals(nul1, nul2)
    }
    
    @Test
    fun `should test for Json array equality`() {
        // given
        val array1 = Json.arrayOf(Json.of("test"), Json.of(42), Json.of(true))
        val array2 = Json.arrayOf(Json.of("test"), Json.of(42), Json.of(true))
        val array3 = Json.arrayOf(Json.of("test"), Json.of(43), Json.of(true))
        val array4 = Json.arrayOf(Json.of(42), Json.of(true), Json.of("test"))



        // then
        assertEquals(array1, array1)
        assertEquals(array1, array2)
        assertNotEquals(array1, array3)
        assertNotEquals(array1, array4)

        assertEquals(array1.hashCode(), array2.hashCode())
    }
    
    @Test
    fun `should test for Json object equality`() {
        // given
        val object1 = Json.objectOf("name" to Json.of("John"), "age" to Json.of(30), "active" to Json.of(true))
        val object2 = Json.objectOf("name" to Json.of("John"), "age" to Json.of(30), "active" to Json.of(true))
        val object3 = Json.objectOf("name" to Json.of("John"), "age" to Json.of(31), "active" to Json.of(true))
        val object4 = Json.objectOf("fullName" to Json.of("John"), "age" to Json.of(30), "active" to Json.of(true))

        // then
        assertEquals(object1, object1)
        assertEquals(object1, object2)
        assertNotEquals(object1, object3)
        assertNotEquals(object1, object4)

        assertEquals(object1.hashCode(), object2.hashCode())
    }
    
    @Test
    fun `should test for Json objects and arrays recursively structured`() {
        //given
        val structure1 = Json.objectOf(
            "id" to Json.of(1),
            "tags" to Json.arrayOf(
                Json.of("tag1"), Json.of("tag2")),
            "metadata" to Json.objectOf(
                "created" to Json.of("2023-01-01"),
                "versions" to Json.arrayOf(
                    Json.objectOf(
                        "version" to Json.of(1.0),
                        "stable" to Json.of(true)),
                )
            ))

        val structure2 = Json.objectOf(
            "id" to Json.of(1),
            "tags" to Json.arrayOf(
                Json.of("tag1"), Json.of("tag2")),
            "metadata" to Json.objectOf(
                "created" to Json.of("2023-01-01"),
                "versions" to Json.arrayOf(
                    Json.objectOf(
                        "version" to Json.of(1.0),
                        "stable" to Json.of(true)),
                )
            ))

        // Different complex structure (change deep in the structure)
        val structure3 = Json.objectOf(
            "id" to Json.of(1),
            "tags" to Json.arrayOf(
                Json.of("tag1"), Json.of("tag2")),
            "metadata" to Json.objectOf(
                "created" to Json.of("2023-01-01"),
                "versions" to Json.arrayOf(
                    Json.objectOf(
                        "version" to Json.of(1.1),
                        "stable" to Json.of(true)),
                )
            ))

        // then
        assertEquals(structure1, structure1)
        assertEquals(structure1, structure2)
        assertNotEquals(structure1, structure3)

        assertEquals(structure1.hashCode(), structure2.hashCode())
    }
}