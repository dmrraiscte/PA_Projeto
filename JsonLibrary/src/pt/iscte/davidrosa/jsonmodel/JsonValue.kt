package pt.iscte.davidrosa.jsonmodel

/**
 * Represents a hierarchy of possible Json value types
 *
 * The complete tye hierarchy includes:
 * - [JsonObject] for key-value pairs
 * - [JsonArray] for ordered collections
 * - [JsonString] for string values
 * - [JsonNumber] for numeric values
 * - [JsonBoolean] for boolean values
 * - [JsonNull] for explicit null values
 *
 * Implementations must support visitor pattern acceptance and serialization
 */
sealed interface JsonValue {
    /**
     * Accepts a [JsonVisitor] to perform operations on this [JsonValue]
     *
     * @param visitor The visitor implementation that will be used to process this value
     * @param parentKey The key of this Json value, if it exists
     * @see JsonVisitor for the visitor interface definition
     */
    fun accept(visitor: JsonVisitor, parentKey: String? = null)

    /**
     * Converts this [JsonValue] into its string representation
     *
     * @return A properly formatted string representation of this value
     */
    fun stringify(): String
}