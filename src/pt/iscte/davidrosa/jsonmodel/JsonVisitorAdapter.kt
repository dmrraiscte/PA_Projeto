package pt.iscte.davidrosa.jsonmodel

/**
 * Base class providing no-op implementations for all visit methods.
 *
 * Typical use-case:
 * ```
 * val prettyPrinter = object : JsonVisitorAdapter() {
 *     override fun visit(obj: JsonObject, parentKey: String?) {
 *         if(parentKey?.contains("some non important text") != false) {
 *             return
 *         } else obj.forEach { key,value ->
 *             println("$key = ${value.stringify()}")
 *         }
 *     }
 * }
 * jsonValue.accept(prettyPrinter)
 * ```
 */
open class JsonVisitorAdapter : JsonVisitor {
    override fun visit(obj: JsonObject, parentKey: String?) {}

    override fun visit(arr: JsonArray, parentKey: String?) {}

    override fun visit(str: JsonString, parentKey: String?) {}

    override fun visit(num: JsonNumber, parentKey: String?) {}

    override fun visit(bool: JsonBoolean, parentKey: String?) {}

    override fun visit(nul: JsonNull, parentKey: String?) {}

}