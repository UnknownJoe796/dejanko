package com.ivieleague.dejanko.orm

@Target(AnnotationTarget.CLASS)
annotation class DjangoPath(
    val appName: String,
    val modelName: String,
    val databaseName: String = "default"
)

@Target(AnnotationTarget.PROPERTY)
annotation class MaxLength(val count: Int)

@Target(AnnotationTarget.PROPERTY)
annotation class PrimaryKey()

@Target(AnnotationTarget.PROPERTY)
annotation class Index()

@Target(AnnotationTarget.PROPERTY)
annotation class OnDelete(val behavior: DeleteBehavior)

enum class DeleteBehavior {
    Cascade, SetNull
}