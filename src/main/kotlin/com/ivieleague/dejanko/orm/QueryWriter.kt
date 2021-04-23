package com.ivieleague.dejanko.orm

import kotlin.math.exp
import kotlin.reflect.KClass

class QueryWriter(val builder: StringBuilder = StringBuilder()) : Appendable by builder {
    val variables = ArrayList<Any?>()
    val tableNameIndex = 0
    fun emitVariable(value: Any?) {
        append('?')
        variables.add(value)
    }
    fun emitType(type: DBType<*>) {
//        return type.sql
    }
    val tables = HashMap<DBTable, Int>()
    var tableNumber = 0
    fun emitTable(table: DBTable) {
        val index = tables.getOrPut(table) { ++tableNumber }
        append('(')
        table.write(this)
        append(" AS t${index}")
        append(')')
    }
    fun emitTableReference(table: DBTable) {
        append("t${tables.getOrPut(table) { ++tableNumber }}")
    }

    override fun toString(): String = builder.toString()
}
