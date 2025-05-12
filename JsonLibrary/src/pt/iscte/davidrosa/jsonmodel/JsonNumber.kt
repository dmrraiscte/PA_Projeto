package pt.iscte.davidrosa.jsonmodel

/**
 * Represents a number value in Json format
 *
 * This class handles the serialization of the Kotlin [Number] values into valid Json numbers.
 * This class is immutable and all operations are pure.
 *
 * @param value The raw number content
 * @constructor Creates a [JsonNumber] wrapper for the given value
 * @see JsonValue for the complete Json value hierarchy
 */
data class JsonNumber(val value : Number) : JsonValue {

    /**
     * Accepts a visitor according to the visitor pattern.
     *
     * @param visitor The [JsonVisitor] implementation that will process this [JsonNumber]
     * @param parentKey The parent key of this Json number, if it exists
     */
    override fun accept(visitor: JsonVisitor, parentKey: String?)  = visitor.visit(this, parentKey)

    /**
     * Serializes this numeric value into its Json string representation.
     *
     * @return The string representation of the number as a Json numeric literal
     */
    override fun stringify(): String = value.toString()

}
