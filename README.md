# JSON Utilities

Made of two modules, JsonLibrary, a type-safe kotlin library for working with JSON data structures, and GetJson, a framework 

## JsonLibrary

A type-safe Kotlin library for working with JSON data structures with a clean, intuitive API.

### Features

- **Type Safety**: Fully leverages Kotlin's type system
- **Immutability**: All JSON values are immutable, promoting predictable behavior
- **Comprehensive API**: Support for all JSON value types (object, array, string, number, boolean, null)
- **Factory Design Pattern**: Simplified creation through the `Json` factory object
- **Visitor Pattern**: Support for traversing and processing JSON structures
- **Powerful Operations**: Filter, map, and transform JSON structures
- **Automatic Conversion**: Convert Kotlin objects to JSON representations
- **Clean Serialization**: Convert JSON structures to properly formatted strings

### Basic Usage

#### Creating JSON Values

```kotlin
import pt.iscte.davidrosa.jsonmodel.Json

// Create JSON values
val jsonString = Json.of("Hello world")
val jsonNumber = Json.of(42)
val jsonBoolean = Json.of(true)
val jsonNull = Json.nullValue()

// Create JSON arrays
val emptyArray = Json.arrayOf()
val stringArray = Json.arrayOf(Json.of("one"), Json.of("two"), Json.of("three"))

// Create JSON objects
val emptyObject = Json.objectOf()
val person = Json.objectOf(
    "name" to Json.of("John Doe"),
    "age" to Json.of(30),
    "isEmployee" to Json.of(true),
    "address" to Json.objectOf(
        "street" to Json.of("123 Main St"),
        "city" to Json.of("Anytown")
    )
)
```

#### Automatic Conversion from Kotlin Types

```kotlin
// Convert Kotlin objects to JSON
val map = mapOf(
    "name" to "Jane Doe",
    "age" to 28,
    "hobbies" to listOf("reading", "hiking", "coding")
)
val jsonFromMap = Json.of(map)

// Convert Kotlin data classes to JSON
data class Person(val name: String, val age: Int, val email: String?)
val jsonFromClass = Json.of(Person("John Smith", 35, null))
```

#### Working with JSON Arrays

```kotlin
val array = Json.arrayOf(
    Json.of(1),
    Json.of(2),
    Json.of(3),
    Json.of(4),
    Json.of(5)
)

// Access elements
val firstElement = array.get(0) // JsonNumber(1)

// Filter elements
val evenNumbers = array.filter { 
    it is JsonNumber && it.value.toInt() % 2 == 0 
}

// Map elements
val doubled = array.map { 
    when (it) {
        is JsonNumber -> Json.of(it.value.toInt() * 2)
        else -> it
    }
}

// Process each element
array.forEach { element ->
    println(element.stringify())
}
```

#### Working with JSON Objects

```kotlin
val person = Json.objectOf(
    "name" to Json.of("John Doe"),
    "age" to Json.of(30),
    "isEmployee" to Json.of(true)
)

// Access properties
val name = person.get("name") // JsonString("John Doe")

// Get object properties
val keys = person.keys // Set of "name", "age", "isEmployee"
val values = person.values // Collection of corresponding JSON values
val entries = person.entries // Set of key-value entries

// Filter properties
val ageOnly = person.filter { it is JsonNumber }

// Iterate over properties
person.forEach { key, value ->
    println("$key: ${value.stringify()}")
}
```

#### Path-based Filtering

```kotlin
val complexObject = Json.objectOf(
    "users" to Json.arrayOf(
        Json.objectOf(
            "id" to Json.of(1),
            "name" to Json.of("Alice"),
            "active" to Json.of(true)
        ),
        Json.objectOf(
            "id" to Json.of(2),
            "name" to Json.of("Bob"),
            "active" to Json.of(false)
        )
    )
)

// Filter based on path
val activeUsers = complexObject.filter { path, value ->
    path.lastOrNull() == "active" && value is JsonBoolean && value.value
}
```

#### Deep Transformations

```kotlin
val array = Json.arrayOf(
    Json.objectOf("value" to Json.of(1)),
    Json.objectOf("value" to Json.of(2)),
    Json.objectOf("value" to Json.of(3))
)

// Transform all numbers in the structure
val transformed = array.deepMap { value ->
    if (value is JsonNumber) Json.of(value.value.toInt() * 10) else value
}
```

#### Visitor Pattern

```kotlin
class CountingVisitor : JsonVisitor {
    var stringCount = 0
    var numberCount = 0
    var booleanCount = 0
    var nullCount = 0
    var objectCount = 0
    var arrayCount = 0
    
    override fun visit(str: JsonString, parentKey: String?) {
        stringCount++
    }
    
    override fun visit(num: JsonNumber, parentKey: String?) {
        numberCount++
    }
    
    override fun visit(bool: JsonBoolean, parentKey: String?) {
        booleanCount++
    }
    
    override fun visit(nul: JsonNull, parentKey: String?) {
        nullCount++
    }
    
    override fun visit(obj: JsonObject, parentKey: String?) {
        objectCount++
    }
    
    override fun visit(arr: JsonArray, parentKey: String?) {
        arrayCount++
    }
}

val jsonValue = Json.of(mapOf("items" to listOf(1, 2, 3), "enabled" to true))
val visitor = CountingVisitor()
jsonValue.accept(visitor)

println("Structure contains:")
println("- ${visitor.objectCount} objects")
println("- ${visitor.arrayCount} arrays")
println("- ${visitor.numberCount} numbers")
println("- ${visitor.stringCount} strings")
println("- ${visitor.booleanCount} booleans")
println("- ${visitor.nullCount} nulls")
```

#### Serialization

```kotlin
val json = Json.objectOf(
    "name" to Json.of("Product"),
    "price" to Json.of(29.99),
    "available" to Json.of(true),
    "tags" to Json.arrayOf(Json.of("new"), Json.of("featured"))
)

val jsonString = json.stringify()
// {"name" : "Product", "price" : 29.99, "available" : true, "tags" : ["new","featured"]}
```

### JSON Type Hierarchy

The library provides a sealed interface hierarchy for type-safe JSON manipulation:

- `JsonValue` (sealed interface): Base for all JSON values
    - `JsonObject`: Represents a JSON object with key-value pairs
    - `JsonArray`: Represents a JSON array (ordered collection)
    - `JsonString`: Represents a JSON string value
    - `JsonNumber`: Represents a JSON numeric value
    - `JsonBoolean`: Represents a JSON boolean value
    - `JsonNull`: Represents a JSON null value (singleton)

## GetJson

A lightweight, annotation-based REST framework for Kotlin that automatically converts controller responses to JSON using the JSON Model Library.

### Features

- **Annotation-driven Development**: Simple annotations to map controllers to HTTP endpoints
- **Path Variable Support**: Extract dynamic parts from URL paths with typed conversion
- **Query Parameter Binding**: Automatically bind query parameters to method parameters
- **JSON Response Serialization**: Automatic conversion of return values to JSON responses
- **Type-Safe Parameter Conversion**: Convert path and query parameters to appropriate Kotlin types
- **Lightweight Design**: Built on Java's HttpServer with minimal dependencies
- **Clean Exception Handling**: Converts exceptions to appropriate HTTP status codes and error messages

### Basic Usage

#### Creating a Controller

```kotlin
import pt.iscte.davidrosa.getjson.*

// Define a controller with a base path
@Mapping("/api/users")
class UserController {
    
    // Map a method to a specific endpoint
    @Mapping("/list")
    fun getUsers(): List<User> {
        // This method will handle GET requests to /api/users/list
        return userRepository.findAll()
    }
    
    // Use path variables for dynamic parts of the URL
    @Mapping("/{id}")
    fun getUserById(@Path("id") userId: Int): User {
        // This method will handle GET requests to /api/users/{id}
        // The {id} part of the URL will be automatically converted to an Int
        return userRepository.findById(userId) ?: throw IllegalArgumentException("User not found")
    }
    
    // Use query parameters for filtering
    @Mapping("/search")
    fun searchUsers(
        @Param("name") name: String,
        @Param("age") age: Int,
        @Param("active") active: Boolean
    ): List<User> {
        // This method will handle GET requests like:
        // /api/users/search?name=John&age=30&active=true
        return userRepository.search(name, age, active)
    }
}
```

#### Starting the Server

```kotlin
import pt.iscte.davidrosa.getjson.GetJson
import kotlin.reflect.KClass

fun main() {
    // Create the server with your controllers
    val server = GetJson(
        UserController::class,
        ProductController::class
    )
    
    // Start the server on port 8080
    server.start(8080)
    
    // Your server is now running!
    println("Server running at http://localhost:8080/")
}
```

#### Returning JSON Data

Any return value from your controller methods will be automatically converted to JSON:

```kotlin
@Mapping("/products")
class ProductController {
    
    @Mapping("/featured")
    fun getFeaturedProducts(): List<Product> {
        val products = listOf(
            Product("Laptop", 999.99, true),
            Product("Smartphone", 699.99, true),
            Product("Headphones", 149.99, true)
        )
        
        // This will be automatically converted to JSON:
        // [{"name":"Laptop","price":999.99,"inStock":true},...]
        return products
    }
    
    @Mapping("/stats")
    fun getStats(): Map<String, Any> {
        // Maps are also automatically converted to JSON objects
        return mapOf(
            "totalProducts" to 42,
            "categories" to listOf("Electronics", "Books", "Clothing"),
            "averagePrice" to 299.99
        )
    }
}
```

### Annotations

#### `@Mapping`

Specifies the URL path for controllers and methods:

```kotlin
// On a class: defines a base path for all methods
@Mapping("/api/orders")
class OrderController {
    // ...
}

// On a method: defines the specific endpoint relative to the base path
@Mapping("/recent")
fun getRecentOrders(): List<Order> {
    // ...
}
```

#### `@Path`

Maps a method parameter to a path variable:

```kotlin
// The {orderId} in the URL will be mapped to the parameter
@Mapping("/{orderId}")
fun getOrder(@Path("orderId") id: Long): Order {
    // ...
}

// If the parameter name matches the path variable name, you can omit the name
@Mapping("/{id}")
fun getOrder(@Path id: Long): Order {
    // ...
}
```

#### `@Param`

Maps a method parameter to a URL query parameter:

```kotlin
// Maps the "sortBy" query parameter to the sortField parameter
@Mapping("/list")
fun listOrders(@Param("sortBy") sortField: String): List<Order> {
    // ...
}

// If the parameter name matches the query parameter name, you can omit the name
@Mapping("/list")
fun listOrders(@Param limit: Int, @Param offset: Int): List<Order> {
    // ...
}
```

### Parameter Type Conversion

The framework automatically converts path and query parameters to the following Kotlin types:

- `String` - No conversion needed
- `Int` - String to Integer conversion
- `Long` - String to Long conversion
- `Double` - String to Double conversion
- `Float` - String to Float conversion
- `Boolean` - String to Boolean conversion ("true"/"false")

### Error Handling

GetJson provides built-in error handling:

- **404 Not Found**: When no route matches the request path
- **405 Method Not Allowed**: When the HTTP method is not supported (currently only GET is supported)
- **400 Bad Request**: When required parameters are missing or cannot be converted
- **500 Internal Server Error**: When an exception occurs during request processing

Errors are returned as JSON objects:

```json
{
  "error_message": "Path variable userId not found"
}
```

### Advanced Examples

#### Combining Path Variables and Query Parameters

```kotlin
@Mapping("/users/{userId}/orders")
fun getUserOrders(
    @Path("userId") id: Long,
    @Param("status") status: String,
    @Param("from") fromDate: String,
    @Param("to") toDate: String
): List<Order> {
    // Handle GET /users/123/orders?status=shipped&from=2023-01-01&to=2023-12-31
}
```

#### Optional Parameters

```kotlin
@Mapping("/products")
fun listProducts(
    @Param("category") category: String,
    @Param("minPrice") minPrice: Double,
    @Param("maxPrice") maxPrice: Double,
    @Param("limit") limit: Int = 20,
    @Param("offset") offset: Int = 0
): List<Product> {
    // Parameters with default values are optional
}
```

### Integration with JSON Model Library

GetJson automatically uses the JSON Model Library to serialize responses:

```kotlin
@Mapping("/complex-data")
fun getComplexData(): Map<String, Any> {
    return mapOf(
        "users" to listOf(
            mapOf("id" to 1, "name" to "Alice"),
            mapOf("id" to 2, "name" to "Bob")
        ),
        "statistics" to mapOf(
            "totalUsers" to 2,
            "activeUsers" to 1
        ),
        "serverTime" to System.currentTimeMillis()
    )
    
    // The framework will convert this to properly formatted JSON
}
```
