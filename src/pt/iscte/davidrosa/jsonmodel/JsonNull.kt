package pt.iscte.davidrosa.jsonmodel

/**
 * Represents the explicit `null` value in Json.
 *
 * This is a singleton object that serializes  to the literal `"null"`.
 *
 * @see JsonValue for the complete Json value hierarchy
 */
data object JsonNull : JsonValue {

    /**
     * Accepts a visitor according to the visitor pattern.
     *
     * @param visitor The [JsonVisitor] implementation that will process this [JsonNull]
     * @param parentKey The parent key of this Json null, if it exists
     */
    override fun accept(visitor: JsonVisitor, parentKey: String?) = visitor.visit(this, parentKey)

    /**
     * Serializes this null value into its Json string representation.
     *
     * @returns The literal string "null" without quotes
     */
    override fun stringify(): String = "null"

}