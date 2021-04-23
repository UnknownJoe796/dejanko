package com.ivieleague.dejanko.orm

import com.github.jasync.sql.db.ResultSet
import com.github.jasync.sql.db.RowData
import com.github.jasync.sql.db.SuspendingConnection
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.jvmErasure

inline fun <reified T: Any> query(
    builder: QueryBuilder.()->Unit = {}
): Query<T> {
    return QueryBuilder(T::class.dbInfo).apply {
        select<T>()
    }.apply(builder).build(SimpleParser<T>(T::class))
}

interface FieldSet<T> {
    val table: DBTable
}

abstract class ModelCompanion<T: Any, F: FieldSet<T>>(val makeFieldSet: (DBTable)->F) {
    val type = this::class.supertypes.find { it.classifier == ModelCompanion::class }!!.arguments[0].type!!.jvmErasure as KClass<T>
    init {
        println("Type is ${type}")
    }
    val info = type.dbInfo
    inline fun query(builder: QueryBuilder.(F) -> Unit): Query<T> {
        return QueryBuilder(info).apply {
            selectAll()
            builder(makeFieldSet(from))
        }.build(SimpleParser<T>(type))
    }
}