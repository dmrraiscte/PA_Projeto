package pt.iscte.davidrosa.getjson

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Mapping(val path: String)

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Path(val name: String = "")

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Param(val name: String = "")