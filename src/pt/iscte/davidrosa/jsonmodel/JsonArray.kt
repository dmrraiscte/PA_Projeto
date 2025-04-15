package pt.iscte.davidrosa.jsonmodel

/**
 * Represents a Json array (an ordered sequence of [JsonValue] elements).
 *
 * This class is immutable. All mutating operations (like [filter] or [map]) return new instances.
 *
 * @property size the number of elements in the array (read-only)
 * @constructor Creates a Json array from a private list, instead use [Json] factory methods or the public constructors
 */
class JsonArray private constructor(private val array : List<JsonValue>) : JsonValue {

    /**
     * The number of elements in the array
     */
    val size : Int get() = array.size

    /**
     * Creates an empty Json array
     */
    constructor() : this(emptyList())

    /**
     * Creates a Json array from a collection of [JsonValue]
     *
     * @param collection The elements to include in the array
     */
    constructor(collection : Collection<JsonValue>) : this(collection.toList())

    /**
     * Accepts a [JsonVisitor] to process this array using the visitor pattern.
     *
     * @param visitor The [JsonVisitor] implementation that will process this [JsonArray]
     * @param parentKey The parent key of this Json array, if it exists
     */
    override fun accept(visitor: JsonVisitor, parentKey: String?) {
        visitor.visit(this, parentKey)
        this.forEach { value ->
            value.accept(visitor)
        }
    }

    /**
     * Serializes the array to its Json string representation
     *
     * @return A string wrapped in `[]` with comma-separated elements
     */
    override fun stringify(): String = array.joinToString(separator = ",", prefix = "[", postfix = "]") { it.stringify() }

    /**
     * Gets the element at the specified index.
     *
     * @param index The position of the element
     * @return The [JsonValue] at that index
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    fun get(index : Int) : JsonValue = if(index in 0..array.lastIndex) array[index] else throw IndexOutOfBoundsException()

    /**
     * Filters elements using a predicate.
     *
     * @param predicate Function that determines which elements to include
     * @return A new [JsonArray] containing only matching elements
     */
    fun filter(predicate: (JsonValue) -> Boolean) : JsonArray {
        val result = mutableListOf<JsonValue>()
        for(value in array) {
            if(predicate(value)) {
                result.add(value)
                continue
            }
            when(value) {
                is JsonArray -> {
                    val filteredArray = value.filter(predicate)
                    if (filteredArray.isNotEmpty()) {
                        result.add(filteredArray)
                    }
                }
                is JsonObject -> {
                    val filteredObject = value.filter(predicate)
                    if (filteredObject.isNotEmpty()) {
                        result.add(filteredObject)
                    }
                }
                else -> {}
            }
        }
        return JsonArray(result.toList())
    }

    /**
     * Filters elements from a Json array based on a pth predicate.
     *
     * This functions serves as the entry point for filtering Json elements with regards not only to the Json's value but also its path of parent keys.
     *
     * @param pathPredicate A function that evaluates which elements should be included in the results
     * @return A [JsonArray] containing all elements that satisfy the predicate, maintaining the hierarchical structure when appropriate
     */
    fun filter(pathPredicate: (List<String>, JsonValue) -> Boolean) : JsonArray {
        return filterWithPath(emptyList(), pathPredicate)
    }

    /**
     * Internal recursive function that performs Json filtering based on a path predicate.
     *
     * This functions traverses the structure recursively, applying the predicate to each element.
     *
     * @param currentPath The current path in the Json structure as a list of Strings
     * @param pathPredicate A function that evaluates which elements should be included in the results
     * @return A [JsonArray] containing all elements that satisfy the predicate, maintaining the hierarchical structure when appropriate
     */
    internal fun filterWithPath(currentPath: List<String>, pathPredicate: (List<String>, JsonValue) -> Boolean) : JsonArray {
        val result = mutableListOf<JsonValue>()

        for(i in array.indices) {
            val element = array[i]
            val elementPath = currentPath + i.toString()

            if(pathPredicate(elementPath, element)) result.add(element)

            when(element) {
                is JsonArray -> {
                    val filteredArray = element.filterWithPath(elementPath,pathPredicate)
                    if(filteredArray.isNotEmpty()) result.add(filteredArray)
                }
                is JsonObject -> {
                    val filteredObject = element.filterWithPath(elementPath,pathPredicate)
                    if(filteredObject.isNotEmpty()) result.add(filteredObject)
                }
                else -> {}
            }
        }

        return JsonArray(result.toList())
    }

    /**
     * Indicates if this [JsonArray] is empty
     *
     * @return A boolean value
     * @see Array.isNotEmpty
     */
    fun isNotEmpty() : Boolean {
        return array.isNotEmpty()
    }

    /**
     * Maps each element in a [JsonArray] to a new value using the provided transform function.
     *
     * @param transform Function that converts each [JsonValue] to a new one
     * @return A new [JsonArray] containing the transformed elements
     */
    fun <T : JsonValue> map(transform : (JsonValue) -> T) : JsonArray {
        val result = mutableListOf<JsonValue>()

        for(element in array) {
            val transformed = transform(element)
            result.add(transformed)
        }

        return JsonArray(result.toList())
    }

    /**
     * Maps each element in a JsonArray to a new value using the provided transform function
     * that has access to both the element and its index.
     *
     * @param transform Function that converts each JsonValue to a new one using its index
     * @return A new [JsonArray] containing the transformed elements
     */
    fun <T : JsonValue> mapIndexed(transform: (index: Int, JsonValue) -> T) : JsonArray {
        val result = mutableListOf<JsonValue>()

        for((index, element) in array.withIndex()) {
            val transformed = transform(index, element)
            result.add(transformed)
        }

        return JsonArray(result.toList())
    }

    /**
     * Recursively maps all elements in a [JsonArray] and its nested structures.
     *
     * Applies the transform function to each element and then recursively to any
     * nested [JsonArray] or [JsonObject] within the transformed results.
     *
     * @param transform Function that converts each JsonValue to another [JsonValue]
     * @return A new [JsonArray] with all elements and nested structures transformed
     */
    fun deepMap(transform : (JsonValue) -> JsonValue ) : JsonArray {
        val result = mutableListOf<JsonValue>()

        for (element in array) {
            val transformed = transform(element)

            val finalElement = when(transformed) {
                is JsonArray -> transformed.deepMap(transform)
                is JsonObject -> transformed.deepMap(transform)
                else -> transformed
            }
            result.add(finalElement)
        }

        return JsonArray(result.toList())
    }

    /**
     * Performs an action for each element
     *
     * @param action The operation to execute on each element
     */
    fun forEach(action : (JsonValue) -> Unit) = array.forEach(action)

    /**
     * Checks if this [JsonArray] is equal to another object.
     *
     * Two [JsonArray] are considered equal only if they are the same instance, or
     * if the other object is also a [JsonArray] with identical array content.
     *
     * @param other The object to compare with this [JsonArray]
     * @return true if the objects are equal, false otherwise
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JsonArray) return false
        return array == other.array
    }

    /**
     * Returns the hash code value for this [JsonArray].
     *
     * The hash code is based on the underlying array's hash code.
     *
     * @return The hash code value
     */
    override fun hashCode(): Int = array.hashCode()

}