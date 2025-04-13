package pt.iscte.davidrosa.jsonmodel

/**
 * Represents a boolean value in Json format.
 *
 * This class handles the serialization of the Kotlin [Boolean] values into valid Json booleans.
 * This class is immutable and all operations are pure.
 *
 * @property value The raw boolean content
 * @constructor Creates a [JsonBoolean] wrapper for the given value
 * @see JsonValue for the complete Json value hierarchy
 */
data class JsonBoolean(val value : Boolean) : JsonValue {

    /**
     * Accepts a visitor according to the visitor pattern.
     *
     * @param visitor The [JsonVisitor] implementation that will process this [JsonBoolean]
     * @param parentKey The parent key of this Json boolean, if it exists
     */
    override fun accept(visitor: JsonVisitor, parentKey: String?) = visitor.visit(this, parentKey)

    /**
     * Converts the boolean into its Json representation
     *
     * @return A valid Json boolean representation
     */
    override fun stringify(): String = value.toString()

    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(other !is JsonBoolean) return false
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

}
