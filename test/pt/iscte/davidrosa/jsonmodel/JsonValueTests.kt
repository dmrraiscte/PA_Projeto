package pt.iscte.davidrosa.jsonmodel

import org.junit.Test
import org.junit.Assert.*
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith

@RunWith(Enclosed::class)
class JsonValueTests {

    class JsonBooleanTests {

        @Test
        fun `should test for all Json boolean possible values`() {
            // given
            val a = JsonBoolean(true)
            val b = JsonBoolean(false)

            // then
            assertEquals("true", a.stringify())
            assertEquals("false", b.stringify())

            assertEquals("true", Json.of(a).stringify())
            assertEquals("false", Json.of(b).stringify())
        }

    }

    class JsonNullTests {

        @Test
        fun `should test for all Json null possibilities`() {
            // given
            val a = JsonNull

            // then
            assertEquals("null", a.stringify())

            assertEquals("null", Json.of(a).stringify())
        }
    }

    class JsonNumberTests {

        @Test
        fun `should test for all Json number formats`() {
            // given
            val a = JsonNumber(1)
            val b = JsonNumber(-2.1)
            val c = JsonNumber(4.5e7)
            val d = JsonNumber(6.872f)
            val e = JsonNumber(0b00001011)
            val f = JsonNumber(0x0F)

            // then
            assertEquals("1", a.stringify())
            assertEquals("-2.1", b.stringify())
            assertEquals("4.5E7", c.stringify())
            assertEquals("6.872", d.stringify())
            assertEquals("11", e.stringify())
            assertEquals("15", f.stringify())

            assertEquals("1", Json.of(a).stringify())
            assertEquals("-2.1", Json.of(b).stringify())
            assertEquals("4.5E7", Json.of(c).stringify())
            assertEquals("6.872", Json.of(d).stringify())
            assertEquals("11", Json.of(e).stringify())
            assertEquals("15", Json.of(f).stringify())
        }
    }

    class JsonStringTests {

        @Test
        fun `should test for proper stringify function of Json strings`() {
            // given
            val a = JsonString("Hello World")
            val b = JsonString("")

            // then
            assertEquals("\"Hello World\"", a.stringify())
            assertEquals("\"\"", b.stringify())

            assertEquals("\"Hello World\"", Json.of(a).stringify())
            assertEquals("\"\"", Json.of(b).stringify())
        }
    }

    class JsonArrayTests {

        @Test
        fun `should test for all possible Json array input formats`() {
            // given
            val a = listOf(JsonString("Hello World"), JsonString("Hello World"))
            val b = arrayOf(JsonString("Hello World"), JsonString("Hello World"))
            val c = setOf(JsonString("Hello World"), JsonString("Hello World"))

            // when
            val d = Json.of(a)
            val e = Json.of(b)
            val f = Json.of(c)
            val g = Json.arrayOf(a)
            val h = Json.arrayOf(c)
            val i = Json.arrayOf(JsonString("Hello World"), JsonString("Hello World"))
            val j = Json.arrayOf(emptyList())
            val k = Json.arrayOf(Json.objectOf("children" to Json.of(5)))

            // then
            assertEquals("[\"Hello World\",\"Hello World\"]", d.stringify())
            assertEquals("[\"Hello World\",\"Hello World\"]", e.stringify())
            assertEquals("[\"Hello World\"]", f.stringify())
            assertEquals("[\"Hello World\",\"Hello World\"]", g.stringify())
            assertEquals("[\"Hello World\"]", h.stringify())
            assertEquals("[\"Hello World\",\"Hello World\"]", i.stringify())
            assertEquals("[]", j.stringify())
            assertEquals("[{\"children\" : 5}]", k.stringify())
        }
    }

    class JsonObjectTests {

        private val a = Json.of("Hello World")
        private val b = Json.of(-7.5)
        private val c = Json.of(false)
        private val d = Json.of(null)
        private val e = Json.arrayOf(JsonString("Hello World"), JsonString("Hello World"))

        @Test
        fun `should test all basic functionality of Json object`() {
            // given
            val z = Json.of(mapOf("primeiro" to a,"segundo" to b,"terceiro" to c,"quarto" to d))

            // then
            assertEquals("{\"primeiro\" : \"Hello World\",\"segundo\" : -7.5,\"terceiro\" : false,\"quarto\" : null}", z.stringify())
        }

        @Test
        fun `should test for json arrays inside json objects`() {
            // given
            val z = Json.objectOf("primeiro" to a, "segundo" to b, "terceiro" to c,"quarto" to d,"quinto" to e)

            // then
            assertEquals("{\"primeiro\" : \"Hello World\",\"segundo\" : -7.5,\"terceiro\" : false,\"quarto\" : null,\"quinto\" : [\"Hello World\",\"Hello World\"]}", z.stringify())
        }

        @Test
        fun `should test for json objects inside json objects`() {
            // given
            val y = Json.objectOf("primeiro" to a, "segundo" to b, "terceiro" to c,"quarto" to d,"quinto" to e)
            val z = Json.objectOf("children" to y)

            // then
            assertEquals("{\"children\" : {\"primeiro\" : \"Hello World\",\"segundo\" : -7.5,\"terceiro\" : false,\"quarto\" : null,\"quinto\" : [\"Hello World\",\"Hello World\"]}}", z.stringify())
        }
    }

    class JsonEqualityTests {

        @Test
        fun `should test for object equality`() {
            // given
            val a = Json.of(1)
            val b = Json.of(1)

            // when


            // then
            assertEquals(a, b)
        }

    }


}