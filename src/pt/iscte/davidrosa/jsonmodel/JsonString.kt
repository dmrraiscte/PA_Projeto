package pt.iscte.davidrosa.jsonmodel

/**
 * Represents a string value in Json format.
 *
 * This class handles the serialization of the Kotlin [String] values into valid Json strings.
 * This class is immutable and all operations are pure.
 *
 * @property value The raw string content
 * @constructor Creates a [JsonString] wrapper for the given value
 * @see JsonValue for the complete Json value hierarchy
 */
data class JsonString(val value : String) : JsonValue {

    /**
     * Accepts a visitor according to the visitor pattern.
     *
     * @param visitor The [JsonVisitor] implementation that will process this [JsonString]
     * @param parentKey The parent key of this Json string, if it exists
     */
    override fun accept(visitor: JsonVisitor, parentKey: String?) = visitor.visit(this, parentKey)

    /**
     * Converts the string into its Json representation.
     *
     * Current implementation limited by special characters inside strings.
     * The returned string will be wrapped in double quotes.
     *
     * @return A valid Json string representation
     */
    override fun stringify(): String = "\"${value}\""

}

