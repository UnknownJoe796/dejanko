package com.ivieleague.dejanko.orm

import com.github.jasync.sql.db.ResultSet
import com.github.jasync.sql.db.RowData
import com.ivieleague.dejanko.LazyMapList
import java.lang.Exception
import kotlin.reflect.KClass

typealias RowDataParser<T> = (ResultSet) -> (RowData) -> T

class TypedResultSet<T>(val resultSet: ResultSet, converter: (RowData) -> T): LazyMapList<RowData, T>(resultSet, converter) {
    fun columnNames(): List<String> = resultSet.columnNames()
}

class SimpleParser<T: Any>(val type: KClass<T>): RowDataParser<T> {
    val info = type.dbInfo
    override fun invoke(set: ResultSet): (RowData) -> T {
        val names = set.columnNames()
        val indexes = info.columns.map {
            val i = names.indexOf(it.columnName)
            if(i == -1) throw IllegalArgumentException("Could not find column for ${it.columnName} out of selected fields ${names}")
            i
        }
        val preAllocatedArray = Array<Any?>(info.columns.size) { null }
        return {
            for(i in info.columns.indices){
                preAllocatedArray[i] = info.columns[i].type.parse(it[indexes[i]])
            }
            info.constructor.call(*preAllocatedArray)
        }
    }
}

class PairParser<A, B>(val first: RowDataParser<A>, val second: RowDataParser<B>): RowDataParser<Pair<A, B>> {
    override fun invoke(set: ResultSet): (RowData) -> Pair<A, B> {
        val f = first(set)
        val s = second(set)
        return { f(it) to s(it) }
    }
}