package com.ivieleague.dejanko

import com.github.jasync.sql.db.ResultSet
import com.github.jasync.sql.db.RowData
import com.github.jasync.sql.db.SuspendingConnection
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

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

data class ForeignKey<KEY, T>(val targetType: KClass<*>, val key: KEY)

data class MediaFile(val path: String)


//suspend inline fun <reified T: DjangoModel<KEY>, reified KEY> SuspendingConnection.query() {
//    this.sendQuery("").rows.
//}

private val infoCache = HashMap<KClass<*>, DBInfo>()
val KClass<*>.dbInfo: DBInfo get() = infoCache.getOrPut(this) {
    val constructor = primaryConstructor!!
    val columns = ArrayList<DBColumn>(constructor.parameters.size)
    var pk: DBColumn? = null
    for(param in constructor.parameters){
        println("Parsing param ${param.name}")
        val column = when(param.type.jvmErasure) {
            ForeignKey::class -> DBColumn(
                parameter = param,
                columnName = param.name!!.snakeCase() + "_" + param.type.arguments[1].type!!.jvmErasure.declaredMemberProperties.find { it.hasAnnotation<PrimaryKey>() }!!.name!!,
                parse = { ForeignKey<Any, Any>(param.type.arguments[1].type!!.jvmErasure, it!!) }
            )
            MediaFile::class -> DBColumn(
                parameter = param,
                columnName = param.name!!.snakeCase(),
                parse = { (it as? String)?.let { MediaFile(it) } }
            )
            else -> DBColumn(
                parameter = param,
                columnName = param.name!!.snakeCase()
            )
        }
        columns.add(column)
        if(this.declaredMemberProperties.find { it.name == param.name }?.hasAnnotation<PrimaryKey>() == true) {
            pk = column
        }
    }
    val pathAnno = findAnnotation<DjangoPath>()
    DBInfo(
        appName = pathAnno?.appName ?: simpleName!!,
        modelName = pathAnno?.modelName ?: simpleName!!,
        databaseName = pathAnno?.databaseName ?: "default",
        primaryKey = pk!!,
        columns = columns,
        constructor = constructor
    )
}

class DBInfo(
    val appName: String,
    val modelName: String,
    val databaseName: String = "default",
    val primaryKey: DBColumn,
    val columns: List<DBColumn>,
    val constructor: KFunction<*>
) {
    fun queryStart(): String = buildString {
        append("SELECT ")
        var first = true
        for(col in columns){
            if(first){
                first = false
            } else {
                append(", ")
            }
            append(col.columnName)
        }
        append(" FROM ")
        append(appName)
        append('_')
        append(modelName)
    }
}

class DBColumn(
    val parameter: KParameter,
    val columnName: String,
    val parse: (Any?)->Any? = { it }
)

inline fun <reified T: Any> ResultSet.parsed(): Sequence<T> {
    val info = T::class.dbInfo
    val names = this.columnNames()
    val indexes = info.columns.map { names.indexOf(it.columnName) }
    return this.asSequence().map {
        info.constructor.call(*info.columns.mapIndexed { index, col -> col.parse(it[indexes[index]]) }.toTypedArray()) as T
    }
}

private val snakeCaseRegex = Regex("[A-Z]")
fun String.snakeCase() = snakeCaseRegex.replace(this) { "_" + it.value.toLowerCase() }
