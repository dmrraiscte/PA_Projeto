interface Stringifiable {
    fun stringify(): String
}

class JSONValue<T : Any?>(private val value: T) : Stringifiable {
    init {
        when (value) {
            is JSONNumber<*>, is JSONObject, is JSONArray<*>, is String, is Boolean, null -> {}
            else -> throw IllegalArgumentException("Value of type ${value!!::class} is not supported!")
        }
    }

    fun getValue(): T {return value}

    override fun stringify(): String {
        return when(value) {
            is String -> "\"$value\""
            is Boolean -> value.toString()
            null -> "null"
            is JSONNumber<*> -> value.stringify()
            is JSONObject -> value.stringify()
            is JSONArray<*> -> value.stringify()
            else -> ""
        }
    }
}

class JSONNumber<T>(private val value: T) : Stringifiable {
    init {
        when (value) {
            is Int, is Double, is Float -> {}
            else -> throw IllegalArgumentException("${value!!::class} is not a valid number format!")
        }
    }

    override fun stringify(): String {
        return value.toString()
    }
}

class JSONObject(
    private val collection : Map<String, JSONValue<*>>
) : Stringifiable {

    override fun stringify(): String {
        val ret = StringBuilder()
        if(collection.isNotEmpty()) {
            ret.append("{\n")
            for ((key, value) in collection) {
                if(value.getValue() is JSONObject) {
                    val str = "    \"$key\" : " + (value.getValue() as JSONObject).stringify().replace("\n","\n    ") + ",\n"
                    ret.append(str)
                } else ret.append("    \"$key\" : ${value.stringify()},\n")
            }
            ret.deleteRange(ret.length - 2, ret.length)
            ret.append("\n}")
        } else ret.append("{ }")
        return ret.toString()
    }

}

class JSONArray<JSONValue : Stringifiable>(
    private val array: List<JSONValue>,
) : Stringifiable {



    override fun stringify(): String {
        return array.joinToString(separator = ", ", prefix = "[ ", postfix = " ]") {it.stringify()}
    }
}
