package com.ivieleague.dejanko.orm

import com.ivieleague.dejanko.snakeCase
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

interface DBTable {
    val name: String
    val columns: List<DBColumn<*>>
    fun write(to: QueryWriter)
}

data class DBTableAliased(
    val table: DBTable,
    val alias: String
): DBTable {

    override val name: String
        get() = alias

    override fun write(to: QueryWriter) {
        to.append('(')
        table.write(to)
        to.append(" AS ")
        to.append(alias)
        to.append(')')
    }

    override val columns: List<DBColumn<*>> = table.columns.map { DBFieldColumn(this, it.columnName, it.type) }
}

data class DBQueryTable(
    val query: Query,
    val alias: String
): DBTable {

    override val name: String
        get() = alias

    override fun write(to: QueryWriter) {
        to.append('(')
        query.write(to)
        to.append(" AS ")
        to.append(alias)
        to.append(')')
    }

    override val columns: List<DBColumn<*>>
        get() = query.select
}