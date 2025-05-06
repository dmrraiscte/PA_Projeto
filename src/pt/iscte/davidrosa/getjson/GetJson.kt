package pt.iscte.davidrosa.getjson

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import pt.iscte.davidrosa.jsonmodel.Json
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

class GetJson(vararg controllers: KClass<*>) {

    private val routeHandlers = mutableMapOf<String, RouteHandler>()
    private val server: HttpServer = HttpServer.create()

    init {
        val validControllers = mutableListOf<KClass<*>>()
        val warnings = mutableListOf<String>()

        controllers.forEach { controller ->
            try {
                if(!controller.hasAnnotation<Controller>()) {
                    warnings.add("WARNING: Class ${controller.simpleName} is not annotated with @Controller and will be skipped.")
                    return@forEach
                }
                val basePath = controller.findAnnotation<Mapping>()?.path ?: ""

                var methodsRegistered = 0

                controller.declaredFunctions.forEach { function ->
                    try {
                        val methodMapping = function.findAnnotation<Mapping>()
                        if (methodMapping != null) {
                            // Combine controller base path and method path
                            val fullPath = normalizePath(basePath, methodMapping.path)

                            // Create instance of controller
                            val controllerInstance = controller.constructors.first().call()

                            // Register the route
                            routeHandlers[fullPath] = RouteHandler(controllerInstance, function)
                            methodsRegistered++
                        }
                    } catch (e: Exception) {
                        warnings.add("WARNING: Failed to register method ${function.name} in ${controller.simpleName}: ${e.message}")
                    }
                }

                if (methodsRegistered > 0) {
                    validControllers.add(controller)
                } else {
                    warnings.add("WARNING: No valid methods with @Mapping found in controller ${controller.simpleName}")
                }

            } catch (e: Exception) {
                warnings.add("WARNING: Failed to process controller ${controller.simpleName}: ${e.message}")
            }
        }

        if (warnings.isNotEmpty()) {
            println("GetJson initialization completed with warnings:")
            warnings.forEach { println(it) }
        }

        server.executor = Executors.newFixedThreadPool(10)
        server.createContext("/") { exchange ->
            handleRequest(exchange)
        }
    }

    fun start(port: Int) {
        //server.stop(0)
        server.bind(InetSocketAddress(port),0)
        server.start()
        println("--- Server started on port $port ---")
    }

    fun stop() {
        server.stop(0)
        println("--- Server stopped ---")
    }

    private fun handleRequest(exchange: HttpExchange) {
        // check request method
        if(exchange.requestMethod != "GET") {
            errorResponse(exchange, 405, "Method ${exchange.requestMethod} not allowed")
            return
        }

        val path = exchange.requestURI.path

        // check if matching route exists
        val (handler, pathVariables) = findMatchingRoutes(path) ?: run {
            errorResponse(exchange, 404, "Route not found")
            return
        }

        try {
            val queryParams = parseQueryParameters(exchange.requestURI.query)

            val result = handler.invoke(pathVariables, queryParams)

            val response = Json.of(result).stringify()
            jsonResponse(exchange, 200, response)
        } catch (ex: Exception) {
            ex.printStackTrace()
            errorResponse(exchange, 500, "Server error: ${ex.message}")
        }
    }

    private fun findMatchingRoutes(path: String): Pair<RouteHandler, Map<String,String>>? {
        // try the basic matching case
        routeHandlers[path]?.let { handler -> return handler to emptyMap() }

        // try for complex paths
        for ((routePattern, handler) in routeHandlers) {
            // for each route handler try to match route path pattern to real path pattern
            val pathVariables = matchPathPattern(routePattern, path)
            if(pathVariables != null) { // if there was a match between route path pattern and real path pattern return
                return handler to pathVariables
            }
        }

        return null
    }

    private fun matchPathPattern(pattern: String, path: String): Map<String, String>? {
        val patternParts = pattern.split("/")
        val pathParts = path.split("/")

        if (patternParts.size != pathParts.size) return null

        val pathVariables = mutableMapOf<String, String>()

        for(i in patternParts.indices) {
            val patternPart = patternParts[i]
            val pathPart = pathParts[i]

            // check if this pattern section is a variable
            if(patternPart.startsWith("{") && patternPart.endsWith("}")) {
                val varName = patternPart.substringAfter("{").substringBefore("}")
                pathVariables[varName] = pathPart
            } else if(patternPart != pathPart) return null
        }

        return pathVariables
    }

    private fun parseQueryParameters(query: String?): Map<String, String> {
        if(query.isNullOrEmpty()) return emptyMap()

        return query.split("&")
            .filter { it.contains("=")}
            .associate { param ->
                val (key,value) = param.split("=", limit=2)
                key to value
            }
    }

    private fun jsonResponse(exchange: HttpExchange, statusCode: Int, response: String) {
        try {
            exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
            exchange.sendResponseHeaders(statusCode, response.length.toLong())
            exchange.responseBody.use { os ->
                os.write(response.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //exchange.responseBody.write(response.toByteArray())
    }

    private fun errorResponse(exchange: HttpExchange, statusCode: Int, message: String) {
        try {
            val response = Json.of(mapOf("error_message" to Json.of(message))).stringify()
            exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
            exchange.sendResponseHeaders(statusCode, response.length.toLong())
            exchange.responseBody.use { os ->
                os.write(response.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        exchange.sendResponseHeaders(statusCode, response.stringify().length.toLong())
//        exchange.responseBody.write(response.stringify().toByteArray())
    }

    private fun normalizePath(basePath: String, methodPath: String): String {
        val normalizedBase = if (basePath.startsWith("/")) basePath else "/$basePath"
        val normalizedMethod = if (methodPath.startsWith("/")) methodPath else "/$methodPath"

        val path = normalizedBase + normalizedMethod
        return if (path == "/") path else path.removeSuffix("/")
    }


    inner class RouteHandler(
        private val controllerInstance: Any,
        private val function: KFunction<*>
    ) {
        fun invoke(pathVariables: Map<String, String>, queryParameters: Map<String, String>): Any? {
            val args = mutableMapOf<KParameter, Any?>()

            function.parameters.forEach { param ->
                // skip "this" param
                if(param.kind == KParameter.Kind.INSTANCE) {
                    args[param] = controllerInstance
                    return@forEach
                }

                // check @path annotation
                val pathAnnotation = param.findAnnotation<Path>()
                if(pathAnnotation != null) {
                    val pathVarName = pathAnnotation.name.ifEmpty { param.name ?: throw IllegalArgumentException("Parameter name not available") }

                    val value = pathVariables[pathVarName] ?: throw IllegalArgumentException("Path variable $pathVarName not found")
                    args[param] = convertToType(value, param)
                    return@forEach
                }

                // check @param annotation
                val paramAnnotation = param.findAnnotation<Param>()
                if(paramAnnotation != null) {
                    val paramName = paramAnnotation.name.ifEmpty { param.name ?: throw IllegalArgumentException("Parameter name not available") }

                    val value = queryParameters[paramName]
                    args[param] = if(value != null) convertToType(value, param) else null
                    return@forEach
                }
            }

            return function.callBy(args)
        }

        private fun convertToType(value: String, parameter: KParameter): Any {
            return when(parameter.type.jvmErasure) {
                String::class -> value
                Int::class -> value.toInt()
                Long::class -> value.toLong()
                Boolean::class -> value.toBoolean()
                Double::class -> value.toDouble()
                Float::class -> value.toFloat()
                else -> throw IllegalArgumentException("Parameter type ${parameter.type} not supported")
            }
        }

    }
}