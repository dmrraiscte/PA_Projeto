package pt.iscte.davidrosa.jsonmodel

import kotlin.reflect.*
import kotlin.reflect.full.memberProperties

/**
 * Factory object for creating [JsonValue] instances with type-safe construction methods.
 *
 * This serves as the primary entry point for creating all Json values in the system.
 * All instantiation should be done through these methods rather than directly using constructors of specific implementations.
 *
 * For known simple types of data, the [of] methods should be used.
 * For [JsonArray] creation the [arrayOf] methods should be used.
 * For [JsonObject] creation the [objectOf] methods should be used.
 * For unknown raw data, the generalized [of] method should be used.
 */
object Json {

    /**
     * Creates a [JsonNumber] from a numeric value.
     *
     * @param number The numeric value (Int, Long, Double, Float, etc.)
     */
    fun of(number : Number) : JsonValue = JsonNumber(number)

    /**
     * Creates a [JsonString] from a string value.
     *
     * @param string The text value
     */
    fun of(string : String) : JsonValue = JsonString(string)

    /**
     * Creates a [JsonBoolean] from a boolean value.
     *
     * @param boolean The boolean value
     */
    fun of(boolean : Boolean) : JsonValue = JsonBoolean(boolean)

    /**
     * Returns the [JsonValue] if it's already a Json value.
     *
     * @param value Existing Json value instance
     */
    fun of(value : JsonValue) : JsonValue = value

    /**
     * Returns the singleton [JsonNull] instance.
     */
    fun nullValue() : JsonValue = JsonNull

    /**
     * Creates an empty [JsonArray].
     */
    fun arrayOf() : JsonArray = JsonArray()

    /**
     * Creates a [JsonArray] from vararg elements.
     *
     * @param elements The Json values to include in the array
     */
    fun arrayOf(vararg elements : JsonValue) : JsonArray = JsonArray(elements.toList())

    /**
     * Creates a [JsonArray] from a collection of elements.
     *
     * @param elements Collection of Json values
     */
    fun arrayOf(elements : Collection<JsonValue>) : JsonArray = JsonArray(elements)

    /**
     * Creates an empty [JsonObject].
     */
    fun objectOf() : JsonObject = JsonObject()

    /**
     * Creates a [JsonObject] from vararg key-value pairs.
     *
     * @param pairs The properties to include in the object
     */
    fun objectOf(vararg pairs : Pair<String, JsonValue>) : JsonObject = JsonObject(*pairs)

    /**
     * Creates a [JsonObject] from a map of properties.
     *
     * @param map The property map
     */
    fun objectOf(map : Map<String, JsonValue>) : JsonObject = JsonObject(map.entries.map { entry -> entry.toPair() })

    /**
     * Creates a [JsonObject] from a collection of key-value pairs.
     *
     * @param collection The property pairs
     */
    fun objectOf(collection : Collection<Pair<String, JsonValue>>) : JsonObject = JsonObject(collection)

    /**
     * Universal factory method that converts any Kotlin value to its Json representation.
     *
     * Supports automatic conversion of:
     * - `null` -> [JsonNull]
     * - Numbers -> [JsonNumber]
     * - Strings -> [JsonString]
     * - Booleans -> [JsonBoolean]
     * - Enums -> [JsonString] (name of the enum constant)
     * - Collections -> [JsonArray] (recursive)
     * - Arrays -> [JsonArray] (recursive)
     * - Maps -> [JsonObject] (recursive; converts keys to strings)
     *
     * @param value The value to convert
     * @return Appropriate Json value representation
     * @throws IllegalArgumentException for unsupported types
     */
    @Suppress("UNCHECKED_CAST")
    fun of(value : Any?) : JsonValue = when(value) {
        null -> JsonNull
        is Number -> JsonNumber(value)
        is String -> JsonString(value)
        is Boolean -> JsonBoolean(value)
        is JsonValue -> value
        is Enum<*> -> JsonString(value.name)
        is Iterable<*> -> arrayOf(value.map { of(it) })
        is Array<*> -> arrayOf(value.map { of(it) })
        is Map<*,*> -> objectOf(value.mapKeys { it.key.toString() }.mapValues { of(it.value) })
        else -> {
            try {
                val kClass = value::class
                val properties = kClass.memberProperties

                val entries = properties.map { prop ->
                    val name = prop.name
                    val propValue = (prop as KProperty1<Any,*>).get(value)
                    name to of(propValue)
                }

                objectOf(entries)
            } catch (e: Exception) {
                throw IllegalArgumentException("Can't convert ${value::class.java} to JsonValue", e)
            }
        }
    }

}