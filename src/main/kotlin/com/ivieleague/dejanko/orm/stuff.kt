package com.ivieleague.dejanko.orm

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


//suspend inline fun <reified T: DjangoModel<KEY>, reified KEY> SuspendingConnection.query() {
//    this.sendQuery("").rows.
//}

inline fun <reified T: Any> ResultSet.parsed(): Sequence<T> = parsed(T::class)
fun <T: Any> ResultSet.parsed(type: KClass<T>): Sequence<T> {
    val info = type.dbInfo
    val names = this.columnNames()
    val indexes = info.columns.map { names.indexOf(it.columnName) }
    return this.asSequence().map {
        val raw = info.columns.mapIndexed { index, col -> col.type.parse(it[indexes[index]]) }
        info.constructor.call(*raw.toTypedArray()) as T
    }
}
