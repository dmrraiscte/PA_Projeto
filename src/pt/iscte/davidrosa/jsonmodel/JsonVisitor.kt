package pt.iscte.davidrosa.jsonmodel

/**
 * Visitor interface for traversing and operating on a [JsonValue] hierarchy.
 *
 * Each `visit()` method corresponds to a specific [JsonValue] type, and, when applicable,
 * its parent key.
 *
 * This parent key only applies when this specific [JsonValue] is directly below a [JsonObject].
 * Otherwise, it's `null`.
 *
 * @see JsonValue
 */
interface JsonVisitor {
    /**
     * Visits a [JsonObject].
     *
     * @param obj The [JsonObject] to be visited
     * @param parentKey The parent key of the `obj`, if it exists
     */
    fun visit(obj: JsonObject, parentKey: String? = null) {}

    /**
     * Visits a [JsonArray].
     *
     * @param arr The [JsonArray] to be visited
     * @param parentKey The parent key of the `arr`, if it exists
     */
    fun visit(arr: JsonArray, parentKey: String? = null) {}

    /**
     * Visits a [JsonString].
     *
     * @param str The [JsonString] to be visited
     * @param parentKey The parent key of the `str`, if it exists
     */
    fun visit(str: JsonString, parentKey: String? = null) {}

    /**
     * Visits a [JsonNumber].
     *
     * @param num The [JsonNumber] to be visited
     * @param parentKey The parent key of the `num`, if it exists
     */
    fun visit(num: JsonNumber, parentKey: String? = null) {}

    /**
     * Visits a [JsonBoolean].
     *
     * @param bool The [JsonBoolean] to be visited
     * @param parentKey The parent key of the `bool`, if it exists
     */
    fun visit(bool: JsonBoolean, parentKey: String? = null) {}

    /**
     * Visits a [JsonNull].
     *
     * @param nul The [JsonNull] to be visited
     * @param parentKey The parent key of the `nul`, if it exists
     */
    fun visit(nul: JsonNull, parentKey: String? = null) {}

}