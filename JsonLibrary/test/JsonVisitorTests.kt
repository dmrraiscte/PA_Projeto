import org.junit.Test
import org.junit.Assert.*
import pt.iscte.davidrosa.jsonmodel.*

class JsonVisitorTests {

    @Test
    fun `should visit all Json value types`() {
        // given
        val visited = mutableListOf<String>()
        val visitor = object : JsonVisitor {
            override fun visit(obj: JsonObject, parentKey: String?) {
                visited.add("Object:${parentKey ?: "root"}")
            }

            override fun visit(arr: JsonArray, parentKey: String?) {
                visited.add("Array:${parentKey ?: "root"}")
            }

            override fun visit(str: JsonString, parentKey: String?) {
                visited.add("String:${parentKey ?: "root"}:${str.value}")
            }

            override fun visit(num: JsonNumber, parentKey: String?) {
                visited.add("Number:${parentKey ?: "root"}:${num.value}")
            }

            override fun visit(bool: JsonBoolean, parentKey: String?) {
                visited.add("Boolean:${parentKey ?: "root"}:${bool.value}")
            }

            override fun visit(nul: JsonNull, parentKey: String?) {
                visited.add("Null:${parentKey ?: "root"}")
            }
        }

        // when
        val jsonObject = Json.objectOf(
            "name" to Json.of("John"),
            "age" to Json.of(30),
            "isActive" to Json.of(true),
            "address" to Json.objectOf(
                "street" to Json.of("Main St"),
                "number" to Json.of(123)
            ),
            "hobbies" to Json.arrayOf(
                Json.of("reading"),
                Json.of("coding")
            ),
            "metadata" to Json.of(null)
        )
        jsonObject.accept(visitor)

        // then
        assertTrue(visited.contains("Object:root"))
        assertTrue(visited.contains("String:name:John"))
        assertTrue(visited.contains("Number:age:30"))
        assertTrue(visited.contains("Boolean:isActive:true"))
        assertTrue(visited.contains("Object:address"))
        assertTrue(visited.contains("String:street:Main St"))
        assertTrue(visited.contains("Number:number:123"))
        assertTrue(visited.contains("Array:hobbies"))
        assertTrue(visited.contains("String:root:reading"))
        assertTrue(visited.contains("String:root:coding"))
        assertTrue(visited.contains("Null:metadata"))
    }

    @Test
    fun `should correctly track parent keys during visitation`() {
        // given
        val keyPaths = mutableListOf<String>()
        val visitor = object : JsonVisitor {
            override fun visit(str: JsonString, parentKey: String?) {
                if (parentKey != null) {
                    keyPaths.add(parentKey)
                }
            }

            override fun visit(num: JsonNumber, parentKey: String?) {
                if (parentKey != null) {
                    keyPaths.add(parentKey)
                }
            }
        }

        // when
        val jsonObject = Json.objectOf(
            "user" to Json.objectOf(
                "profile" to Json.objectOf(
                    "firstName" to Json.of("Jane"),
                    "lastName" to Json.of("Doe"),
                    "age" to Json.of(28)
                ),
                "settings" to Json.objectOf(
                    "theme" to Json.of("dark"),
                    "notifications" to Json.of(true)
                )
            )
        )
        jsonObject.accept(visitor)

        // then
        assertTrue(keyPaths.contains("firstName"))
        assertTrue(keyPaths.contains("lastName"))
        assertTrue(keyPaths.contains("age"))
        assertTrue(keyPaths.contains("theme"))
        assertEquals(4, keyPaths.size)
    }

    @Test
    fun `should implement a visitor that collects strings`() {
        // given
        val stringCollector = object : JsonVisitor {
            val strings = mutableListOf<String>()

            override fun visit(str: JsonString, parentKey: String?) {
                strings.add(str.value)
            }
        }

        // when
        val complex = Json.objectOf(
            "id" to Json.of(1),
            "title" to Json.of("Meeting notes"),
            "tags" to Json.arrayOf(
                Json.of("work"),
                Json.of("important"),
                Json.of("planning")
            ),
            "participants" to Json.arrayOf(
                Json.objectOf(
                    "name" to Json.of("Alice"),
                    "role" to Json.of("Manager")
                ),
                Json.objectOf(
                    "name" to Json.of("Bob"),
                    "role" to Json.of("Developer")
                )
            ),
            "data" to Json.objectOf(
                "duration" to Json.of(60),
                "location" to Json.of("Conference Room A")
            )
        )
        complex.accept(stringCollector)

        // then
        val expected = listOf(
            "Meeting notes",
            "work", "important", "planning",
            "Alice", "Manager",
            "Bob", "Developer",
            "Conference Room A"
        )
        assertEquals(expected.size, stringCollector.strings.size)
        for (str in expected) {
            assertTrue("Should contain: $str", stringCollector.strings.contains(str))
        }
    }

    @Test
    fun `should implement a path tracking visitor`() {
        // given
        val pathTracker = object : JsonVisitor {
            val paths = mutableMapOf<String, Any>()
            private val currentPath = mutableListOf<String>()

            override fun visit(obj: JsonObject, parentKey: String?) {
                if (parentKey != null) {
                    currentPath.add(parentKey)
                }

                val checkpoint = currentPath.toList()

                obj.forEach { key, value ->
                    value.accept(this, key)
                }

                currentPath.clear()
                currentPath.addAll(checkpoint)

                if (parentKey != null && currentPath.isNotEmpty()) {
                    currentPath.removeAt(currentPath.lastIndex)
                }
            }

            override fun visit(arr: JsonArray, parentKey: String?) {
                if (parentKey != null) {
                    currentPath.add(parentKey)
                }

                val checkpoint = currentPath.toList()

                for (i in 0 until arr.size) {
                    currentPath.add(i.toString())
                    arr.get(i).accept(this)
                    currentPath.removeAt(currentPath.lastIndex)
                }

                currentPath.clear()
                currentPath.addAll(checkpoint)

                if (parentKey != null && currentPath.isNotEmpty()) {
                    currentPath.removeAt(currentPath.lastIndex)
                }
            }

            override fun visit(str: JsonString, parentKey: String?) {
                if (parentKey != null) {
                    currentPath.add(parentKey)
                }
                val path = currentPath.joinToString(".")
                paths[path] = str.value
                if (parentKey != null && currentPath.isNotEmpty()) {
                    currentPath.removeAt(currentPath.lastIndex)
                }
            }

            override fun visit(num: JsonNumber, parentKey: String?) {
                if (parentKey != null) {
                    currentPath.add(parentKey)
                }
                val path = currentPath.joinToString(".")
                paths[path] = num.value
                if (parentKey != null && currentPath.isNotEmpty()) {
                    currentPath.removeAt(currentPath.lastIndex)
                }
            }
        }

        // when
        val jsonObject = Json.objectOf(
            "user" to Json.objectOf(
                "name" to Json.of("John"),
                "contacts" to Json.arrayOf(
                    Json.objectOf("type" to Json.of("email"), "value" to Json.of("john@example.com")),
                    Json.objectOf("type" to Json.of("phone"), "value" to Json.of("555-1234"))
                )
            )
        )
        jsonObject.accept(pathTracker)

        // then
        assertEquals("John", pathTracker.paths["user.name"])
        assertEquals("email", pathTracker.paths["user.contacts.0.type"])
        assertEquals("john@example.com", pathTracker.paths["user.contacts.0.value"])
        assertEquals("phone", pathTracker.paths["user.contacts.1.type"])
        assertEquals("555-1234", pathTracker.paths["user.contacts.1.value"])
    }
}