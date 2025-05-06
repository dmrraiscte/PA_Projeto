import org.junit.Assert.*
import org.junit.Test
import java.net.http.HttpClient
import pt.iscte.davidrosa.getjson.*

fun main() {
    val app = GetJson(UserController::class, ProductController::class)
    app.start(8080)
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

        //@BeforeAll
        @JvmStatic
        fun setupServer() {
            app = GetJson(UserController::class, TestController::class, ErrorController::class)
            app.start(TEST_PORT)
            println("Test server started on port $TEST_PORT")
        }

        //@AfterAll
        @JvmStatic
        fun tearDownServer() {
            app.stop()
            println("Test server stopped")
        }
    }

}