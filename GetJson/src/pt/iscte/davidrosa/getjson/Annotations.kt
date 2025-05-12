package pt.iscte.davidrosa.getjson

/**
 * Annotation for mapping controllers and methods to URL paths.
 *
 * This annotation can be applied to classes and functions to define the URL path
 * at which they should be accessible. When applied to a class, it defines a base path
 * for all methods in the class.
 *
 * Example:
 * ```kotlin
 * @Mapping("/api/users")
 * class UserController {
 *     @Mapping("/list")
 *     fun getAllUsers(): List<User> {
 *         // This method will be accessible at /api/users/list
 *     }
 * }
 * ```
 *
 * @property path The URL path to map to this controller or method */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Mapping(val path: String)

/**
 * Annotation for mapping method parameters to path variables.
 *
 * Path variables are parts of the URL path that are variable and can be extracted
 * as parameters. They are specified in the [Mapping] path using curly braces.
 *
 * Example:
 * ```kotlin
 * @Mapping("/users/{userId}")
 * fun getUser(@Path("userId") id: Int): User {
 *     // The 'id' parameter will receive the value from the URL path
 *     // e.g., for "/users/42", id will be 42
 * }
 * ```
 *
 * @property name The name of the path variable in the URL pattern.
 *            If empty, the parameter name will be used. */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Path(val name: String = "")

/**
 * Annotation for mapping method parameters to query parameters.
 *
 * Query parameters are specified in the URL after the '?' character.
 *
 * Example:
 * ```kotlin
 * @Mapping("/users")
 * fun searchUsers(@Param("name") userName: String?): List<User> {
 *     // For a request to "/users?name=John", userName will be "John"
 * }
 * ```
 *
 * @property name The name of the query parameter.
 *            If empty, the parameter name will be used. */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Param(val name: String = "")