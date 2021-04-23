package com.ivieleague.dejanko.orm

import com.ivieleague.dejanko.snakeCase
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

interface DBTable {
    val sourceClass: KClass<*>? get() = null
    val columns: List<DBColumn<*>>
    fun <T> columnForProperty(property: KProperty1<*, T>): DBColumn<T>? = null
    fun write(to: QueryWriter)
}

data class DBQueryTable(
    val query: Query<*>,
    val alias: String
): DBTable {
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

class DBAliasTable(
    val table: DBTable
): DBTable {
    override val sourceClass: KClass<*>? get() = table.sourceClass

    class DBJoinColumn<T>(
        val parent: DBAliasTable,
        val original: DBColumn<T>
    ): DBFieldColumn<T>(parent, original.columnName, original.type) {
        override val sourceProperty: KProperty1<*, *>?
            get() = original.sourceProperty
    }

    override val columns: List<DBColumn<*>>
    get() = table.columns.map { DBJoinColumn(this, it) }

    private val byProperty: Map<KProperty1<*, *>, DBColumn<*>> by lazy {
        columns.filter { it.sourceProperty != null }.associateBy { it.sourceProperty!! }
    }

    override fun write(to: QueryWriter) = table.write(to)

    @Suppress("UNCHECKED_CAST")
    override fun <T> columnForProperty(property: KProperty1<*, T>): DBColumn<T>? {
        return byProperty[property] as? DBColumn<T>
    }
}