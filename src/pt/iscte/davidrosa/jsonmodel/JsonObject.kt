package pt.iscte.davidrosa.jsonmodel

/**
 * Represents a Json object (a collection of key-vale pairs where the keys are strings).
 *
 * This class is immutable. All mutating operations return new instances.
 *
 * @property size The number of properties in the object (read-only)
 * @property keys the set of all property names (read-only)
 * @constructor Creates a Json object from a private map, instead use [Json] factory methods or the public constructors
 */
class JsonObject private constructor(private val properties : Map<String, JsonValue>) : JsonValue {

    /**
     * The number of properties in this Json object.
     */
    val size : Int get() = properties.size

    /**
     * The set of all property names (keys) in this object.
     */
    val keys : Set<String> get() = properties.keys

    /**
     * The collection of all property values in this object.
     */
    val values : Collection<JsonValue> get() = properties.values

    /**
     * The set of all property entries.
     */
    val entries : Set<Map.Entry<String, JsonValue>> get() = properties.entries

    /**
     * Creates an empty Json object.
     */
    constructor() : this(emptyMap())

    /**
     * Creates a Json object from vararg key-value pairs.
     * @param pairs The properties to include in the object
     */
    constructor(vararg pairs : Pair<String, JsonValue>) : this(pairs.toMap())

    /**
     * Creates a Json object from a collection of key-value pairs.
     *
     * @param map The properties to include in the object
     */
    constructor(map : Collection<Pair<String, JsonValue>>) : this(map.toMap())

    /**
     * Accepts a [JsonVisitor] to process this object using the visitor pattern.
     *
     * @param visitor The visitor implementation
     * @param parentKey The parent key of this Json object, if it exists
     */
    override fun accept(visitor: JsonVisitor, parentKey: String?) {
        visitor.visit(this, parentKey)
        this.forEach { key,value ->
            value.accept(visitor, key)
        }
    }

    /**
     * Serializes the object to its Json string representation.
     *
     * @return A string wrapped in `{}` with comma-separated key-value pairs
     */
    override fun stringify(): String {
        return properties.entries.joinToString(",","{","}") { (key,value) -> "\"$key\" : ${value.stringify()}" }
    }

    /**
     * Gets the value associated with a key.
     *
     * @param key The property name to look up
     * @return The Json value if the key exists, null otherwise
     */
    fun get(key : String) : JsonValue? = properties[key]

    /**
     * Filters properties using a predicate.
     *
     * @param predicate Function that determines which properties to keep
     * @return A new [JsonObject] containing only matching properties
     */
    fun filter(predicate: (JsonValue) -> Boolean) : JsonObject {
        val result = mutableMapOf<String, JsonValue>()
        for((key,value) in properties) {
            if (predicate(value)) {
                result[key] = value
                continue
            }
            when(value) {
                is JsonArray -> {
                    val filteredArray = value.filter(predicate)
                    if (filteredArray.isNotEmpty()) {
                        result[key] = filteredArray
                    }
                }
                is JsonObject -> {
                    val filteredObject = value.filter(predicate)
                    if (filteredObject.isNotEmpty()) {
                        result[key] = filteredObject
                    }
                }
                else -> {}
            }
        }
        return JsonObject(result.toMap())
    }

    fun filter(pathPredicate: (List<String>,JsonValue) -> Boolean) : JsonObject {
        return filterWithPath(emptyList(), pathPredicate)
    }

    internal fun filterWithPath(currentPath: List<String>,pathPredicate: (List<String>,JsonValue) -> Boolean) : JsonObject {
        val result = mutableMapOf<String, JsonValue>()

        for((key,value) in properties) {
            val valuePath = currentPath + key

            if(pathPredicate(valuePath,value)) result[key] = value

            when(value) {
                is JsonArray -> {
                    val filteredArray = value.filterWithPath(valuePath,pathPredicate)
                    if(filteredArray.isNotEmpty()) result[key] = filteredArray
                }
                is JsonObject -> {
                    val filteredObject = value.filterWithPath(valuePath,pathPredicate)
                    if(filteredObject.isNotEmpty()) result[key] = filteredObject
                }
                else -> {}
            }
        }

        return JsonObject(result.toMap())
    }

    fun deepMap(transform: (JsonValue) -> JsonValue) : JsonObject {
        val result = mutableMapOf<String, JsonValue>()

        for((key,value) in properties) {
            val transformedValue = transform(value)

            val finalValue = when(transformedValue) {
                is JsonArray -> transformedValue.deepMap(transform)
                is JsonObject -> transformedValue.deepMap(transform)
                else -> transformedValue
            }
            result[key] = finalValue
        }

        return JsonObject(result.toMap())
    }

    /**
     * Indicates if this [JsonArray] is empty
     *
     * @return A boolean value
     * @see Map.isNotEmpty
     */
    fun isNotEmpty() : Boolean {
        return properties.isNotEmpty()
    }

    /**
     * Performs an action on each property.
     *
     * @param action The operation to be performed on each key-value pair
     */
    fun forEach(action : (String, JsonValue) -> Unit) = properties.forEach(action)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JsonObject) return false
        return properties == other.properties
    }

    override fun hashCode(): Int = properties.hashCode()

}