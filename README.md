# JSON Model Library

A type-safe Kotlin library for working with JSON data structures with a clean, intuitive API.

## Overview

This library provides a complete implementation of JSON data representation in Kotlin with a focus on type safety, immutability, and clean API design. It supports all standard JSON data types including objects, arrays, strings, numbers, booleans, and null values.

The library features:

- Immutable implementations of all JSON data types
- Type-safe construction through a factory API
- Visitor pattern support for traversing and processing JSON structures
- Powerful filtering capabilities for both values and paths
- Deep transformations of complex nested structures
- Clean serialization to standard JSON string format

## Core Components

### JsonValue Interface

The base interface for all JSON values with common functionality:

| Method | Description |
|--------|-------------|
| `stringify()` | Converts the JSON value to its string representation |
| `accept(visitor, parentKey)` | Accepts a visitor for processing |

### Json Factory

The `Json` object provides factory methods for creating all JSON value types:

| Method | Description | Example |
|--------|-------------|---------|
| `of(number: Number)` | Creates a JsonNumber | `Json.of(42)` |
| `of(string: String)` | Creates a JsonString | `Json.of("hello")` |
| `of(boolean: Boolean)` | Creates a JsonBoolean | `Json.of(true)` |
| `of(value: JsonValue)` | Returns the provided value | `Json.of(existingJsonValue)` |
| `nullValue()` | Creates a JsonNull | `Json.nullValue()` |
| `arrayOf()` | Creates an empty JsonArray | `Json.arrayOf()` |
| `arrayOf(vararg elements: JsonValue)` | Creates a JsonArray from values | `Json.arrayOf(Json.of(1), Json.of("text"))` |
| `arrayOf(elements: Collection<JsonValue>)` | Creates a JsonArray from a collection | `Json.arrayOf(listOfJsonValues)` |
| `objectOf()` | Creates an empty JsonObject | `Json.objectOf()` |
| `objectOf(vararg pairs: Pair<String, JsonValue>)` | Creates a JsonObject from pairs | `Json.objectOf("key" to Json.of("value"))` |
| `objectOf(map: Map<String, JsonValue>)` | Creates a JsonObject from a map | `Json.objectOf(mapOfJsonValues)` |
| `objectOf(collection: Collection<Pair<String, JsonValue>>)` | Creates a JsonObject from pairs | `Json.objectOf(pairsOfJsonValues)` |
| `of(value: Any?)` | Smart conversion from Kotlin values | `Json.of(mapOf("a" to 1, "b" to listOf(2, 3)))` |

Example of using the factory methods:

```kotlin
// Creating simple values
val number = Json.of(42)
val string = Json.of("hello")
val boolean = Json.of(true)
val nullVal = Json.nullValue()

// Creating an array
val array = Json.arrayOf(number, string, boolean, nullVal)

// Creating an object
val object = Json.objectOf(
    "number" to number,
    "string" to string,
    "boolean" to boolean,
    "null" to nullVal,
    "array" to array
)

// Smart conversion from Kotlin data structures
val inferred = Json.of(
    mapOf(
        "items" to listOf(1, 2, 3),
        "info" to mapOf("name" to "test")
    )
)
```

### JsonNumber

Represents a numeric value in JSON.

| Property/Method | Description | Example |
|-----------------|-------------|---------|
| `value: Number` | The underlying number value | `jsonNumber.value` |
| `stringify()` | Returns the string representation | `jsonNumber.stringify()` |

Example:
```kotlin
val number = Json.of(42)
println(number.stringify()) // Output: 42
```

### JsonString

Represents a string value in JSON.

| Property/Method | Description | Example |
|-----------------|-------------|---------|
| `value: String` | The underlying string value | `jsonString.value` |
| `stringify()` | Returns the string representation with quotes | `jsonString.stringify()` |

Example:
```kotlin
val string = Json.of("hello")
println(string.stringify()) // Output: "hello"
```

### JsonBoolean

Represents a boolean value in JSON.

| Property/Method | Description | Example |
|-----------------|-------------|---------|
| `value: Boolean` | The underlying boolean value | `jsonBoolean.value` |
| `stringify()` | Returns the string representation | `jsonBoolean.stringify()` |

Example:
```kotlin
val boolean = Json.of(true)
println(boolean.stringify()) // Output: true
```

### JsonNull

Represents the null value in JSON.

| Method | Description | Example |
|--------|-------------|---------|
| `stringify()` | Returns the string representation | `JsonNull.stringify()` |

Example:
```kotlin
val nullValue = Json.nullValue()
println(nullValue.stringify()) // Output: null
```

### JsonArray

Represents an ordered collection of JSON values.

| Property/Method | Description | Example |
|-----------------|-------------|---------|
| `size: Int` | Number of elements | `jsonArray.size` |
| `get(index: Int)` | Gets element at index | `jsonArray.get(0)` |
| `forEach(action: (JsonValue) -> Unit)` | Performs action on each element | `jsonArray.forEach { println(it) }` |
| `filter(predicate: (JsonValue) -> Boolean)` | Filters elements | `jsonArray.filter { it is JsonNumber }` |
| `filter(pathPredicate: (List<String>, JsonValue) -> Boolean)` | Filters elements based on path | `jsonArray.filter { path, _ -> path.contains("0") }` |
| `isNotEmpty()` | Checks if array is not empty | `if (jsonArray.isNotEmpty()) { ... }` |
| `map(transform: (JsonValue) -> T)` | Maps elements to new values | `jsonArray.map { Json.of(it.stringify()) }` |
| `mapIndexed(transform: (index: Int, JsonValue) -> T)` | Maps elements with index | `jsonArray.mapIndexed { i, v -> Json.of("$i: ${v.stringify()}") }` |
| `deepMap(transform: (JsonValue) -> JsonValue)` | Maps all elements recursively | `jsonArray.deepMap { if (it is JsonNumber) Json.of(it.value.toInt() * 2) else it }` |
| `stringify()` | Returns the string representation | `jsonArray.stringify()` |

Example:
```kotlin
// Creating an array
val array = Json.arrayOf(Json.of(1), Json.of(2), Json.of(3))

// Accessing elements
val firstElement = array.get(0)

// Filtering
val filteredArray = array.filter { it is JsonNumber && (it.value as Number).toInt() > 1 }

// Mapping
val mappedArray = array.map { Json.of((it as JsonNumber).value.toInt() * 2) }

// Iterating
array.forEach { println(it.stringify()) }

// Converting to string
val jsonString = array.stringify() // [1,2,3]
```

### JsonObject

Represents a collection of key-value pairs where keys are strings.

| Property/Method | Description | Example |
|-----------------|-------------|---------|
| `size: Int` | Number of properties | `jsonObject.size` |
| `keys: Set<String>` | Set of property names | `jsonObject.keys` |
| `values: Collection<JsonValue>` | Collection of property values | `jsonObject.values` |
| `entries: Set<Map.Entry<String, JsonValue>>` | Set of property entries | `jsonObject.entries` |
| `get(key: String)` | Gets value by key | `jsonObject.get("name")` |
| `forEach(action: (String, JsonValue) -> Unit)` | Performs action on each property | `jsonObject.forEach { k, v -> println("$k: ${v.stringify()}") }` |
| `filter(predicate: (JsonValue) -> Boolean)` | Filters properties by value | `jsonObject.filter { it is JsonNumber }` |
| `filter(pathPredicate: (List<String>, JsonValue) -> Boolean)` | Filters by path | `jsonObject.filter { path, _ -> path.contains("nested") }` |
| `isNotEmpty()` | Checks if object is not empty | `if (jsonObject.isNotEmpty()) { ... }` |
| `stringify()` | Returns the string representation | `jsonObject.stringify()` |

Example:
```kotlin
// Creating an object
val person = Json.objectOf(
    "name" to Json.of("John"),
    "age" to Json.of(30),
    "isActive" to Json.of(true),
    "address" to Json.objectOf(
        "city" to Json.of("New York"),
        "zip" to Json.of("10001")
    )
)

// Accessing properties
val name = person.get("name")
val city = person.get("address")?.let { 
    (it as JsonObject).get("city") 
}

// Filtering
val onlyStringProps = person.filter { it is JsonString }
val addressProps = person.filter { path, _ -> path.contains("address") }

// Iterating
person.forEach { key, value ->
    println("$key: ${value.stringify()}")
}

// Converting to string
val jsonString = person.stringify()
```

## Visitor Pattern

The library implements the visitor pattern to process JSON structures without modifying them.

### JsonVisitor Interface

Defines methods to visit each JSON value type:

| Method | Description |
|--------|-------------|
| `visit(obj: JsonObject, parentKey: String?)` | Visit a JSON object |
| `visit(arr: JsonArray, parentKey: String?)` | Visit a JSON array |
| `visit(str: JsonString, parentKey: String?)` | Visit a JSON string |
| `visit(num: JsonNumber, parentKey: String?)` | Visit a JSON number |
| `visit(bool: JsonBoolean, parentKey: String?)` | Visit a JSON boolean |
| `visit(nul: JsonNull, parentKey: String?)` | Visit a JSON null |

### JsonVisitorAdapter

A base class with empty implementations for all visitor methods.

Example:
```kotlin
// Create a visitor that prints all string values
val stringPrinter = object : JsonVisitorAdapter() {
    override fun visit(str: JsonString, parentKey: String?) {
        println("String value: ${str.value} (parent key: $parentKey)")
    }
}

// Apply the visitor to a JSON structure
jsonValue.accept(stringPrinter)
```

## Advanced Usage Examples

### Deeply Nested Filtering

```kotlin
val data = Json.objectOf(
    "users" to Json.arrayOf(
        Json.objectOf(
            "id" to Json.of(1),
            "name" to Json.of("Alice"),
            "roles" to Json.arrayOf(Json.of("admin"), Json.of("user"))
        ),
        Json.objectOf(
            "id" to Json.of(2),
            "name" to Json.of("Bob"),
            "roles" to Json.arrayOf(Json.of("user"))
        )
    )
)

// Find all admin users
val admins = data.filter { value ->
    value is JsonString && value.value == "admin"
}
```

### Path-Based Operations

```kotlin
val data = Json.objectOf(
    "config" to Json.objectOf(
        "database" to Json.objectOf(
            "host" to Json.of("localhost"),
            "port" to Json.of(5432)
        ),
        "api" to Json.objectOf(
            "url" to Json.of("https://api.example.com"),
            "key" to Json.of("secret-key")
        )
    )
)

// Extract only database configuration
val dbConfig = data.filter { path, _ ->
    path.size >= 2 && path[0] == "config" && path[1] == "database"
}
```
