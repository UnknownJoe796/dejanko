package com.ivieleague.dejanko.orm

import com.github.jasync.sql.db.ResultSet
import com.github.jasync.sql.db.RowData
import com.ivieleague.dejanko.LazyMapList
import kotlin.reflect.KClass

class TypedResultSet<T>(val resultSet: ResultSet, converter: (RowData) -> T): LazyMapList<RowData, T>(resultSet, converter) {
    fun columnNames(): List<String> = resultSet.columnNames()
}

class SimpleParser<T: Any>(val type: KClass<T>): (ResultSet)->TypedResultSet<T> {
    val info = type.dbInfo
    override fun invoke(set: ResultSet): TypedResultSet<T> {
        val names = set.columnNames()
        val indexes = info.columns.map { names.indexOf(it.columnName) }
        val preAllocatedArray = Array<Any?>(info.columns.size) { null }
        return TypedResultSet(set) {
            for(i in info.columns.indices){
                preAllocatedArray[i] = info.columns[i].type.parse(it[indexes[i]])
            }
            info.constructor.call(*preAllocatedArray)
        }
    }
}