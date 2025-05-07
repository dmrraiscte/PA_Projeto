import org.junit.AfterClass
import org.junit.Assert.*
import org.junit.Assert.fail
import org.junit.BeforeClass
import org.junit.Test
import java.net.http.HttpClient
import pt.iscte.davidrosa.getjson.*
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main() {
    val app = GetJson(UserController::class, ProductController::class, TestController::class, InvalidController::class, AnotherInvalidController::class)
    app.start(8080)
}

@Controller
class InvalidController {

}

class AnotherInvalidController {
    @Mapping("unregistered")
    fun test(): String = "This should not be registered"
}

@Controller
@Mapping("api")
class UserController {
    @Mapping("ints")
    fun demo(): List<Int> = listOf(1, 2, 3)

    @Mapping("pair")
    fun obj(): Pair<String, String> = Pair("um", "dois")

    @Mapping("path/{pathvar}")
    fun path(
        @Path pathvar: String
    ): String = "$pathvar!"

    @Mapping("args")
    fun args(
        @Param n: Int,
        @Param text: String
    ): Map<String, String> = mapOf(text to text.repeat(n))
}

@Controller
@Mapping("test")
class TestController {
    @Mapping("hello")
    fun hello(): String = "Hello, World!"

    @Mapping("number")
    fun number(): Int = 42

    @Mapping("list")
    fun list(): List<Int> = listOf(1, 2, 3, 4, 5)

    @Mapping("greet/{name}")
    fun greet(@Path name: String): String = "Hello, $name!"

    @Mapping("users/{userId}/books/{bookId}")
    fun getUserBook(
        @Path userId: Int,
        @Path bookId: Int
    ): Map<String, Int> = mapOf("userId" to userId, "bookId" to bookId)

    @Mapping("repeat")
    fun repeat(
        @Param text: String,
        @Param count: Int
    ): Map<String, String> = mapOf("result" to text.repeat(count))

    @Mapping("sum")
    fun sum(
        @Param a: Int,
        @Param(name = "b") b: Int = 0
    ): Int = a + b

    // This method should be ignored by the framework
    fun ignored(): String = "This should not be accessible"
}

@Controller
@Mapping("error")
class ErrorController {
    @Mapping("throw")
    fun throwException(): String {
        throw RuntimeException("Test exception")
    }
}

@Controller
@Mapping("products")
class ProductController {
    private val products = mapOf(
        1 to Product(1, "Laptop", 999.99),
        2 to Product(2, "Smartphone", 699.99),
        3 to Product(3, "Headphones", 149.99)
    )

    @Mapping("")
    fun getAllProducts(): List<Product> = products.values.toList()

    @Mapping("{id}")
    fun getProduct(@Path id: Int): Product? = products[id]
}

data class Product(val id: Int, val name: String, val price: Double)

class GetJsonTests() {

    companion object {
        private const val TEST_PORT = 8081
        private const val BASE_URL = "http://localhost:$TEST_PORT"
        private lateinit var app: GetJson
        private val client = HttpClient.newBuilder().build()

        @BeforeClass
        @JvmStatic
        fun setupServer() {
            app = GetJson(TestController::class, ErrorController::class)
            app.start(TEST_PORT)
            println("Test server started on port $TEST_PORT")
        }

        @AfterClass
        @JvmStatic
        fun tearDownServer() {
            app.stop()
            println("Test server stopped")
        }
    }

    private fun get(endpoint: String): HttpResponse<String> {
        val request = HttpRequest.newBuilder()
            .uri(URI("$BASE_URL$endpoint"))
            .GET()
            .build()

        return try {
            client.send(request, HttpResponse.BodyHandlers.ofString())
        } catch (e: Exception) {
            fail("Request to $endpoint failed: ${e.message}")
            throw AssertionError("Code not reachable!")
        }
    }

    @Test
    fun `simple endpoint returns correct string data`() {
        val response = get("/test/hello")

        assertEquals(200, response.statusCode())
        assertEquals("\"Hello, World!\"", response.body())
    }

    @Test
    fun `endpoint returns correct number data`() {
        val response = get("/test/number")

        assertEquals(200, response.statusCode())
        assertEquals("42", response.body())
    }

    @Test
    fun `endpoint returns correct list data`() {
        val response = get("/test/list")

        assertEquals(200, response.statusCode())
        assertEquals("[1,2,3,4,5]", response.body().replace("\\s".toRegex(), ""))
    }

    @Test
    fun `path variables are correctly extracted and used`() {
        val response = get("/test/greet/John")

        assertEquals(200, response.statusCode())
        assertEquals("\"Hello, John!\"", response.body())
    }

    @Test
    fun `multiple path variables are correctly extracted and used`() {
        val response = get("/test/users/42/books/123")

        assertEquals(200, response.statusCode())
        val body = response.body()
        assertTrue((body.contains("\"userId\" : 42") && body.contains("\"bookId\" : 123")))
    }

    @Test
    fun `query parameters are correctly extracted and used`() {
        val response = get("/test/repeat?text=hello&count=3")

        assertEquals(200, response.statusCode())
        assertTrue(response.body().contains("\"result\" : \"hellohellohello\""))
    }

    @Test
    fun `optional query parameters can be omitted`() {
        val response = get("/test/sum?a=5&b=3")
        assertEquals(200, response.statusCode())
        assertEquals("8", response.body())

        val responseWithDefault = get("/test/sum?a=5")
        assertEquals(200, responseWithDefault.statusCode())
        assertEquals("5", responseWithDefault.body())
    }

    @Test
    fun `non-existent endpoints return 404`() {
        val response = get("/invalid/endpoint")
        assertEquals(404, response.statusCode())
        assertTrue(response.body().contains("error"))
    }

    @Test
    fun `error controller correctly handles exceptions`() {
        val response = get("/error/throw")
        assertEquals(500, response.statusCode())
        assertTrue(response.body().contains("error"))
    }

    @Test
    fun `invalid parameter type causes expected error`() {
        val response = get("/test/sum?a=five&b=3")
        assertEquals(500, response.statusCode())
        assertTrue(response.body().contains("error"))
    }

    @Test
    fun `controller class methods without annotations are ignored`() {
        val response = get("/test/ignored")
        assertEquals(404, response.statusCode())
    }

}