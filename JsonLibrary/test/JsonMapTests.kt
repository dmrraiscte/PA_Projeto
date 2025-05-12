import org.junit.Test
import org.junit.Assert.*
import pt.iscte.davidrosa.jsonmodel.*

class JsonMapTests {

    @Test
    fun `should map elements in a JsonArray`() {
        // given
        val array = Json.arrayOf(
            Json.of(1),
            Json.of(2),
            Json.of(3),
            Json.of("string")
        )

        // when
        val mapped = array.map { value ->
            when (value) {
                is JsonNumber -> Json.of(value.value.toInt() * 2)
                else -> value
            }
        }

        // then
        val expected = Json.arrayOf(
            Json.of(2),
            Json.of(4),
            Json.of(6),
            Json.of("string"),
        )
        assertEquals(expected, mapped)
    }

    @Test
    fun `should map indexed elements in a JsonArray`() {
        // given
        val array = Json.arrayOf(
            Json.of("a"),
            Json.of("b"),
            Json.of("c")
        )

        // when
        val mapped = array.mapIndexed { index, value ->
            when (value) {
                is JsonString -> Json.of("${value.value}${index}")
                else -> value
            }
        }

        // then
        val expected = Json.arrayOf(
            Json.of("a0"),
            Json.of("b1"),
            Json.of("c2")
        )
        assertEquals(expected, mapped)
    }

    @Test
    fun `should deeply map elements in nested structures`() {
        // given
        val complex = Json.objectOf(
            "numbers" to Json.arrayOf(
                Json.of(1),
                Json.of(2),
                Json.of(3)
            ),
            "nested" to Json.objectOf(
                "value" to Json.of(4),
                "items" to Json.arrayOf(
                    Json.of(5),
                    Json.of(6)
                )
            )
        )

        // when
        val mapped = complex.deepMap { value ->
            when (value) {
                is JsonNumber -> Json.of(value.value.toInt() * 10)
                else -> value
            }
        }

        // then
        val expected = Json.objectOf(
            "numbers" to Json.arrayOf(
                Json.of(10),
                Json.of(20),
                Json.of(30)
            ),
            "nested" to Json.objectOf(
                "value" to Json.of(40),
                "items" to Json.arrayOf(
                    Json.of(50),
                    Json.of(60)
                )
            )
        )
        assertEquals(expected, mapped)
    }

    @Test
    fun `should transform different value types during mapping`() {
        // given
        val array = Json.arrayOf(
            Json.of(1),
            Json.of("hello"),
            Json.of(true),
            Json.of(null)
        )

        // when
        val mapped = array.map { value ->
            when (value) {
                is JsonNumber -> Json.of(value.value.toInt().toString())
                is JsonString -> Json.of(value.value.uppercase())
                is JsonBoolean -> Json.of(!value.value)
                is JsonNull -> Json.of("WAS_NULL")
                else -> value
            }
        }

        // then
        val expected = Json.arrayOf(
            Json.of("1"),
            Json.of("HELLO"),
            Json.of(false),
            Json.of("WAS_NULL")
        )
        assertEquals(expected, mapped)
    }

    @Test
    fun `should handle complex mapping in deep nested structures`() {
        // given
        val user = Json.objectOf(
            "name" to Json.of("John Doe"),
            "active" to Json.of(true),
            "profile" to Json.objectOf(
                "age" to Json.of(30),
                "contact" to Json.objectOf(
                    "email" to Json.of("john@example.com"),
                    "phone" to Json.of(null)
                )
            ),
            "tags" to Json.arrayOf(
                Json.of("customer"),
                Json.of("vip"),
                Json.arrayOf(
                    Json.of("internal"),
                    Json.of("verified")
                )
            )
        )

        // when
        val mapped = user.deepMap { value ->
            when (value) {
                is JsonString -> {
                    if (value.value.contains("@")) {
                        Json.of("[EMAIL REDACTED]")
                    } else {
                        Json.of(value.value.uppercase())
                    }
                }
                is JsonNull -> Json.of("N/A")
                else -> value
            }
        }

        // then
        val expected = Json.objectOf(
            "name" to Json.of("JOHN DOE"),
            "active" to Json.of(true),
            "profile" to Json.objectOf(
                "age" to Json.of(30),
                "contact" to Json.objectOf(
                    "email" to Json.of("[EMAIL REDACTED]"),
                    "phone" to Json.of("N/A")
                )
            ),
            "tags" to Json.arrayOf(
                Json.of("CUSTOMER"),
                Json.of("VIP"),
                Json.arrayOf(
                    Json.of("INTERNAL"),
                    Json.of("VERIFIED")
                )
            )
        )
        assertEquals(expected, mapped)
    }

    @Test
    fun `should map JSON values conditionally based on path`() {
        // given
        val jsonData = Json.objectOf(
            "users" to Json.arrayOf(
                Json.objectOf(
                    "id" to Json.of(1),
                    "name" to Json.of("Alice"),
                    "email" to Json.of("alice@example.com"),
                    "sensitive" to Json.of("secret-data-1")
                ),
                Json.objectOf(
                    "id" to Json.of(2),
                    "name" to Json.of("Bob"),
                    "email" to Json.of("bob@example.com"),
                    "sensitive" to Json.of("secret-data-2")
                )
            ),
            "metadata" to Json.objectOf(
                "createdAt" to Json.of("2023-04-15"),
                "accessKey" to Json.of("super-secret-key")
            )
        )

        val pathVisitor = object : JsonVisitor {
            val result = mutableMapOf<String, JsonValue>()
            private val currentPath = mutableListOf<String>()

            override fun visit(obj: JsonObject, parentKey: String?) {
                if (parentKey != null) currentPath.add(parentKey)
                val pathBackup = ArrayList(currentPath)
                obj.forEach { key, value ->
                    value.accept(this, key)
                }
                currentPath.clear()
                currentPath.addAll(pathBackup)
                if (parentKey != null && currentPath.isNotEmpty()) {
                    currentPath.removeAt(currentPath.lastIndex)
                }
            }

            override fun visit(arr: JsonArray, parentKey: String?) {
                if (parentKey != null) currentPath.add(parentKey)
                val pathBackup = ArrayList(currentPath)

                for (i in 0 until arr.size) {
                    currentPath.add(i.toString())
                    arr.get(i).accept(this)
                    currentPath.removeAt(currentPath.lastIndex)
                }

                currentPath.clear()
                currentPath.addAll(pathBackup)
                if (parentKey != null && currentPath.isNotEmpty()) {
                    currentPath.removeAt(currentPath.lastIndex)
                }
            }

            override fun visit(str: JsonString, parentKey: String?) {
                if (parentKey != null) currentPath.add(parentKey)
                val path = currentPath.joinToString(".")

                val mappedValue = if (path.contains("email") || path.contains("sensitive") || path.contains("accessKey")) {
                    Json.of("[REDACTED]")
                } else {
                    str
                }

                result[path] = mappedValue
                if (parentKey != null && currentPath.isNotEmpty()) {
                    currentPath.removeAt(currentPath.lastIndex)
                }
            }
        }

        // then
        jsonData.accept(pathVisitor)

        assertTrue(pathVisitor.result.containsKey("users.0.email"))
        assertEquals(Json.of("[REDACTED]"), pathVisitor.result["users.0.email"])
        assertEquals(Json.of("[REDACTED]"), pathVisitor.result["users.0.sensitive"])
        assertEquals(Json.of("[REDACTED]"), pathVisitor.result["users.1.email"])
        assertEquals(Json.of("[REDACTED]"), pathVisitor.result["users.1.sensitive"])
        assertEquals(Json.of("[REDACTED]"), pathVisitor.result["metadata.accessKey"])

        assertEquals(Json.of("Alice"), pathVisitor.result["users.0.name"])
        assertEquals(Json.of("Bob"), pathVisitor.result["users.1.name"])
        assertEquals(Json.of("2023-04-15"), pathVisitor.result["metadata.createdAt"])
    }
}