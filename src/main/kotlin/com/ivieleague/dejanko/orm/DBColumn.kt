package com.ivieleague.dejanko.orm

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1


interface DBColumn<T>: DBExpression<T> {
    val columnName: String
    val type: DBType<T>
    fun writeSelect(to: QueryWriter)
}
class DBFieldColumn<K: Any, T>(
    val parentType: KClass<K>,
    val parameter: KParameter,
    val property: KProperty1<K, T>,
    override val columnName: String,
    override val type: DBType<T>
): DBColumn<T> {
    override fun write(to: QueryWriter) {
        to.append(parentType.dbInfo.tableName)
        to.append('.')
        to.append(columnName)
    }
    override fun writeSelect(to: QueryWriter) {
        write(to)
    }
}
class DBExpressionColumn<T>(
    override val columnName: String,
    override val type: DBType<T>,
    val expression: DBExpression<T>
): DBColumn<T> {
    override fun write(to: QueryWriter) {
        to.append(columnName)
    }
    override fun writeSelect(to: QueryWriter) {
        expression.write(to)
        to.append(" AS ")
        to.append(columnName)
    }
}