package pt.iscte.davidrosa.getjson

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure
import pt.iscte.davidrosa.jsonmodel.Json

/**
 * A REST framework that maps controller classes to HTTP endpoints and responds in JSON format.
 *
 * @param controllers The controller classes to register for handling HTTP requests
 */
class GetJson(vararg controllers: KClass<*>) {

    private val routeHandlers = mutableMapOf<String, RouteHandler>()
    private val server: HttpServer = HttpServer.create()

    /**
     * Initializes the server and registers all route handlers from the provided controllers.
     *
     * Any errors during registration are collected and printed as warnings.
     */
    init {
        val validControllers = mutableListOf<KClass<*>>()
        val warnings = mutableListOf<String>()

        controllers.forEach { controller ->
            try {
                val basePath = controller.findAnnotation<Mapping>()?.path ?: ""

                var methodsRegistered = 0

                val controllerInstance = controller.constructors.first().call()

                controller.declaredFunctions.forEach { function ->
                    try {
                        val methodMapping = function.findAnnotation<Mapping>()
                        if (methodMapping != null) {
                            val fullPath = normalizePath(basePath, methodMapping.path)

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

    /**
     * Starts the HTTP server on the specified port.
     *
     * @param port The port number on which the server will listen for incoming requests
     */
    fun start(port: Int) {
        server.bind(InetSocketAddress(port),0)
        server.start()
        println("--- Server started on port $port ---")
    }

    /**
     * Stops the HTTP server.
     */
    fun stop() {
        server.stop(0)
        println("--- Server stopped ---")
    }

    /**
     * Main request handler that processes all incoming HTTP requests.
     *
     * This method:
     * 1. Verifies the request method is GET
     * 2. Matches the request path against registered routes
     * 3. Extracts path variables and query parameters
     * 4. Invokes the appropriate controller method
     * 5. Serializes the result to Json and sends the response
     *
     * @param exchange The HttpExchange containing the request and response objects
     */
    private fun handleRequest(exchange: HttpExchange) {
        if(exchange.requestMethod != "GET") {
            errorResponse(exchange, 405, "Method ${exchange.requestMethod} not allowed")
            return
        }

        val path = exchange.requestURI.path

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

    /**
     * Finds a matching route handler for the given request path.
     *
     * This method first tries to find an exact match, then looks for pattern-based routes with path variables.
     *
     * @param path The request URI path
     * @return A pair containing the route handler and a map of path variables, or null if no match is found
     */
    private fun findMatchingRoutes(path: String): Pair<RouteHandler, Map<String,String>>? {
        routeHandlers[path]?.let { handler -> return handler to emptyMap() }

        for ((routePattern, handler) in routeHandlers) {
            val pathVariables = matchPathPattern(routePattern, path)
            if(pathVariables != null) {
                return handler to pathVariables
            }
        }

        return null
    }

    /**
     * Matches a request path against a pattern path that may contain path variables.
     *
     * Path variables in the pattern are denoted by curly braces.
     * If the pattern matches the path, this method returns a map of variable names to their values.
     *
     * @param pattern The route pattern with possible path variables
     * @param path The actual request path
     * @return A map of path variable names to values, or null if the pattern doesn't match
     */
    private fun matchPathPattern(pattern: String, path: String): Map<String, String>? {
        val patternParts = pattern.split("/")
        val pathParts = path.split("/")

        if (patternParts.size != pathParts.size) return null

        val pathVariables = mutableMapOf<String, String>()

        for(i in patternParts.indices) {
            val patternPart = patternParts[i]
            val pathPart = pathParts[i]

            if(patternPart.startsWith("{") && patternPart.endsWith("}")) {
                val varName = patternPart.substringAfter("{").substringBefore("}")
                pathVariables[varName] = pathPart
            } else if(patternPart != pathPart) return null
        }

        return pathVariables
    }

    /**
     * Parses query parameters from the query string.
     *
     * @param query The query string from the request URI
     * @return A map of parameter names to values
     */
    private fun parseQueryParameters(query: String?): Map<String, String> {
        if(query.isNullOrEmpty()) return emptyMap()

        return query.split("&")
            .filter { it.contains("=")}
            .associate { param ->
                val (key,value) = param.split("=", limit=2)
                key to value
            }
    }

    /**
     * Sends a Json response to the client.
     *
     * @param exchange The HttpExchange to use for the response
     * @param statusCode The HTTP status code to return
     * @param response The Json string to send
     */
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
    }

    /**
     * Sends an error response to the client in Json format.
     *
     * @param exchange The HttpExchange to use for the response
     * @param statusCode The HTTP error status code to return
     * @param message The error message
     */
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
    }

    /**
     * Normalizes and combines base path and method path into a full route path.
     *
     * @param basePath The base path from the controller class annotation
     * @param methodPath The path from the method annotation
     * @return A normalized full path
     */
    private fun normalizePath(basePath: String, methodPath: String): String {
        val normalizedBase = if (basePath.startsWith("/")) basePath else "/$basePath"
        val normalizedMethod = if (methodPath.startsWith("/")) methodPath else "/$methodPath"

        val path = normalizedBase + normalizedMethod
        return if (path == "/") path else path.removeSuffix("/")
    }


    /**
     * Handler class that executes controller methods with properly mapped parameters.
     *
     * This inner class is responsible for invoking the controller method with the
     * appropriate parameters extracted from path variables and query parameters.
     *
     * @property controllerInstance The instance of the controller class
     * @property function The controller method to invoke
     */
    inner class RouteHandler(
        private val controllerInstance: Any,
        private val function: KFunction<*>
    ) {
        /**
         * Invokes the controller method with the given path variables and query parameters.
         *
         * This method:
         * 1. Maps path variables to parameters annotated with [Path]
         * 2. Maps query parameters to parameters annotated with [Param]
         * 3. Converts parameter values to the appropriate types
         * 4. Invokes the controller method with the mapped parameters
         *
         * @param pathVariables Map of path variable names to values
         * @param queryParameters Map of query parameter names to values
         * @return The result of the controller method invocation
         * @throws IllegalArgumentException If a required parameter is missing or parameter type is not supported
         */
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
                    if (value != null) {
                        args[param] = convertToType(value, param)
                    } else if (!param.isOptional) {
                        throw IllegalArgumentException("Required parameter '$paramName' is missing")
                    }
                    return@forEach
                }
            }

            return function.callBy(args)
        }

        /**
         * Converts a string value to the appropriate type for a parameter.
         *
         * @param value The string value to convert
         * @param parameter The parameter with type information
         * @return The converted value
         * @throws IllegalArgumentException If the parameter is not supported
         */
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