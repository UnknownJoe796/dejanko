package com.ivieleague.dejanko.orm

import com.ivieleague.dejanko.snakeCase
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure


private val infoCache = HashMap<KClass<*>, DBInfo<*>>()
private val pkCache = HashMap<KClass<*>, KProperty1<*, *>>()
private val columnCache = HashMap<KProperty1<*, *>, DBFieldColumn<*, *>>()

@Suppress("UNCHECKED_CAST")
val <K: Any, V> KProperty1<K, V>.dbColumn: DBFieldColumn<K, V> get() = columnCache[this] as DBFieldColumn<K, V>

@Suppress("UNCHECKED_CAST")
val <K: Any> KClass<K>.pkInfo: KProperty1<K, *> get() = pkCache.getOrPut(this) {
    memberProperties.find { it.hasAnnotation<PrimaryKey>() } as KProperty1<K, *>
} as KProperty1<K, *>

@Suppress("UNCHECKED_CAST")
val <K: Any> KClass<K>.dbInfo: DBInfo<K> get() = infoCache.getOrPut(this) {
    val constructor = primaryConstructor!!
    val columns = ArrayList<DBFieldColumn<K, *>>(constructor.parameters.size)
    var pk: DBFieldColumn<K, *>? = null
    for(param in constructor.parameters){
        val type = param.type.dbType() as DBType<Any>
        println("Parsing param ${param.name}, type: ${type}, name: ${type.getColumnName(param.name!!.snakeCase())}")
        val column = DBFieldColumn(
            parentType = this,
            parameter = param,
            property = memberProperties.find { it.name == param.name } as KProperty1<K, Any>,
            columnName = type.getColumnName(param.name!!.snakeCase()),
            type = type
        )
        columns.add(column)
        columnCache[column.property] = column
        if(column.property.hasAnnotation<PrimaryKey>()) {
            pk = column
        }
    }
    val pathAnno = findAnnotation<DjangoPath>()
    DBInfo<K>(
        type = this,
        appName = pathAnno?.appName ?: simpleName!!,
        modelName = pathAnno?.modelName ?: simpleName!!,
        databaseName = pathAnno?.databaseName ?: "default",
        primaryKey = pk ?: throw IllegalArgumentException("Type ${this.simpleName} has no primary key"),
        columns = columns,
        constructor = constructor
    )
} as DBInfo<K>

class DBInfo<T: Any>(
    val type: KClass<T>,
    val appName: String,
    val modelName: String,
    val databaseName: String = "default",
    val primaryKey: DBFieldColumn<T, *>,
    val columns: List<DBFieldColumn<T, *>>,
    val constructor: KFunction<T>
) {
    val tableName: String get() = "${appName}_$modelName"

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