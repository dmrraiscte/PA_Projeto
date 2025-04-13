package pt.iscte.davidrosa.jsonmodel

import org.junit.Test
import org.junit.Assert.*

class JsonFilterTests {

    @Test
    fun `should test Json object filtering`() {
        // given
        val obj = Json.objectOf("a" to Json.of(1), "b" to Json.objectOf("c" to Json.of(2), "d" to Json.of(3)))
        val obj2 = Json.objectOf("nome" to Json.of("joao"), "idade" to Json.of(32), "ativo" to Json.of(true))

        // when
        val predicate = { value : JsonValue ->
            value is JsonNumber && value.value.toDouble() > 2
        }

        // then
        val filtered = obj.filter(predicate)
        val filtered2 = obj2.filter {value : JsonValue -> value is JsonString}
        assertEquals(Json.objectOf("b" to Json.objectOf("d" to Json.of(3))), filtered)
        assertEquals(Json.objectOf("nome" to Json.of("joao")),filtered2)
    }

    @Test
    fun `should test Json array filtering`() {
        // given
        val array = Json.arrayOf(Json.of(1),Json.arrayOf(Json.of(2),Json.of(3)),Json.of(4))
        val array2 = Json.arrayOf(Json.of("text"),Json.of(2),Json.of(false),Json.of(null))

        // when
        val predicate = { value: JsonValue ->
            value is JsonNumber && value.value.toDouble() > 2
        }

        // then
        val filtered = array.filter(predicate)
        val filtered2 = array2.filter { value -> value is JsonNumber }
        assertEquals(Json.arrayOf(Json.arrayOf(Json.of(3)),Json.of(4)),filtered)
        assertEquals(Json.arrayOf(Json.of(2)),filtered2)
    }
    
    @Test
    fun `should test filtering of Json objects inside an array`() {
        // given
        val arr = Json.arrayOf(
            Json.objectOf("name" to Json.of("Alice"),"age" to Json.of(25)),
            Json.objectOf("name" to Json.of("Bob"),"age" to Json.of(30))
        )
        val complexObj = Json.objectOf(
            "id" to Json.of(1),
            "pessoa" to Json.objectOf(
                "nome" to Json.of("Maria"),
                "idade" to Json.of(25),
                "contactos" to Json.arrayOf(
                    Json.objectOf(
                        "tipo" to Json.of("email"),
                        "valor" to Json.of("maria@email.com")
                    ),
                    Json.objectOf(
                        "tipo" to Json.of("telefone"),
                        "valor" to Json.of(123456789)
                    )
                )
            ),
            "tags" to Json.arrayOf(
                Json.of("cliente"),
                Json.of("vip"),
                Json.of(42)
            )
        )
        
        // when
        val predicate = { value: JsonValue ->
            value is JsonObject && value.get("age")?.let {
                it is JsonNumber && it.value.toDouble() > 26
            } == true
        }

        val emailsOnly = complexObj.filter { value : JsonValue ->
            (value is JsonString && value.value.contains("@")) or
            (value is JsonObject && value.keys.contains("tipo") && value.get("tipo") is JsonString && (value.get("tipo") as JsonString).value == "email")
        }
        
        // then
        val filtered = arr.filter(predicate)
        assertEquals(Json.arrayOf(
            Json.objectOf("name" to Json.of("Bob"),"age" to Json.of(30))),
            filtered)
        assertEquals(Json.objectOf("pessoa" to Json.objectOf("contactos" to Json.arrayOf(Json.objectOf("tipo" to Json.of("email"), "valor" to Json.of("maria@email.com"))))), emailsOnly)

    }
}