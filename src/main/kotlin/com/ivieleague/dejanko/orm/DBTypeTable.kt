package com.ivieleague.dejanko.orm

import com.ivieleague.dejanko.snakeCase
import kotlin.reflect.*
import kotlin.reflect.full.*


private val infoCache = HashMap<KClass<*>, DBTypeTable<*>>()
private val pkCache = HashMap<KClass<*>, KProperty1<*, *>>()
private val columnCache = HashMap<KProperty1<*, *>, DBTypeTable.Column<*, *>>()

@Suppress("UNCHECKED_CAST")
val <K: Any, V> KProperty1<K, V>.dbColumn: DBTypeTable.Column<K, V> get() = columnCache[this] as DBTypeTable.Column<K, V>

@Suppress("UNCHECKED_CAST")
val <K: Any> KClass<K>.pkInfo: KProperty1<K, *> get() = pkCache.getOrPut(this) {
    memberProperties.find { it.hasAnnotation<PrimaryKey>() } as KProperty1<K, *>
} as KProperty1<K, *>

@Suppress("UNCHECKED_CAST")
val <K: Any> KClass<K>.dbInfo: DBTypeTable<K> get() {
    infoCache[this]?.let { return it as DBTypeTable<K> }
    val typeTable = DBTypeTable<K>(this)
    infoCache[this] = typeTable
    val pathAnno = findAnnotation<DjangoPath>()
    typeTable.appName = pathAnno?.appName ?: simpleName!!
    typeTable.modelName = pathAnno?.modelName ?: simpleName!!
    typeTable.databaseName = pathAnno?.databaseName ?: "default"
    typeTable.constructor = primaryConstructor!!

    for(param in typeTable.constructor.parameters){
        println("Parsing param ${param.name}")
        val type = param.type.dbType() as DBType<Any>
        val column = DBTypeTable.Column(
            parent = typeTable,
            parameter = param,
            property = memberProperties.find { it.name == param.name } as KProperty1<K, Any>,
            columnName = type.getColumnName(param.name!!.snakeCase()),
            type = type
        )
        typeTable.columns.add(column)
        columnCache[column.property] = column
        if(column.property.hasAnnotation<PrimaryKey>()) {
            typeTable.primaryKey = column
        }
    }
    return typeTable
}

class DBTypeTable<T: Any>(val type: KClass<T>): DBTable {
    var appName: String = ""
    var modelName: String = ""
    var databaseName: String = "default"
    lateinit var primaryKey: Column<T, *>
    override val columns = ArrayList<Column<T, *>>()
    lateinit var constructor: KFunction<T>

    override val name: String get() = "${appName}_$modelName"

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

    override fun write(to: QueryWriter) {
        to.append(name)
    }

    class Column<K: Any, T>(
        val parent: DBTypeTable<K>,
        val parameter: KParameter,
        val property: KProperty1<K, T>,
        columnName: String,
        type: DBType<T>
    ): DBFieldColumn<T>(source = parent, columnName = columnName, type = type) {
        override fun write(to: QueryWriter) {
            to.append(parent.name)
            to.append('.')
            to.append(columnName)
        }
        override fun writeSelect(to: QueryWriter) {
            write(to)
        }
    }
}