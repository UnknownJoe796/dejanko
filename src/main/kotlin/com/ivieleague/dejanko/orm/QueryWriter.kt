package com.ivieleague.dejanko.orm

import kotlin.math.exp
import kotlin.reflect.KClass

class QueryWriter(val builder: StringBuilder = StringBuilder()) : Appendable by builder {
    val variables = ArrayList<Any?>()
    fun emitVariable(value: Any?) {
        append('?')
        variables.add(value)
    }
    fun emitType(type: DBType<*>) {
//        return type.sql
    }
}
