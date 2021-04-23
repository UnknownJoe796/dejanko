package com.ivieleague.dejanko.orm

import com.github.jasync.sql.db.ResultSet
import com.github.jasync.sql.db.RowData
import com.ivieleague.dejanko.type
import kotlin.math.exp
import kotlin.reflect.KProperty1

class QueryBuilder(val from: DBTable) {
    val select = ArrayList<DBColumn<*>>()
    val joins = ArrayList<Join>()
    var where: DBExpression<Boolean>? = null
    val orderBy: ArrayList<Sort> = ArrayList()
    var limit: Int? = null
    var offset: Int? = null
    var distinct: Boolean = false
    var groupBy: DBExpression<*>? = null

    fun <T> build(parse: RowDataParser<T>) = Query(
        select = select,
        from = from,
        joins = joins,
        where = where,
        orderBy = orderBy,
        limit = limit,
        offset = offset,
        distinct = distinct,
        groupBy = groupBy,
        parse = parse
    )

    fun selectAll() {
        select.addAll(from.columns)
    }

    inline fun <reified T: Any> select() {
        select.addAll(T::class.dbInfo.columns)
    }

    inline fun <reified T> select(key: String, expression: DBExpression<T>): DBExpressionColumn<T> {
        val col = DBExpressionColumn<T>(key, type = dbType<T>(), expression = expression)
        select.add(col)
        return col
    }

    inline fun <reified T: Any, FS: FieldSet<T>> leftJoin(model: ModelCompanion<T, FS>, on: (set: FS)->DBExpression<Boolean>): FS {
        val newTable = DBAliasTable(model.info)
        val fs = model.makeFieldSet(newTable)
        val join = Join(
            table = newTable,
            on = on(fs),
            kind = JoinKind.LEFT
        )
        joins.add(join)
        return fs
    }

    inline fun <reified T: Any, FS: FieldSet<T>> rightJoin(model: ModelCompanion<T, FS>, on: (set: FS)->DBExpression<Boolean>): FS {
        val newTable = DBAliasTable(model.info)
        val fs = model.makeFieldSet(newTable)
        val join = Join(
            table = newTable,
            on = on(fs),
            kind = JoinKind.RIGHT
        )
        joins.add(join)
        return fs
    }

    inline fun <reified T: Any, FS: FieldSet<T>> innerJoin(model: ModelCompanion<T, FS>, on: (set: FS)->DBExpression<Boolean>): FS {
        val newTable = DBAliasTable(model.info)
        val fs = model.makeFieldSet(newTable)
        val join = Join(
            table = newTable,
            on = on(fs),
            kind = JoinKind.INNER
        )
        joins.add(join)
        return fs
    }

    inline fun <reified T: Any, FS: FieldSet<T>> outerJoin(model: ModelCompanion<T, FS>, on: (set: FS)->DBExpression<Boolean>): FS {
        val newTable = DBAliasTable(model.info)
        val fs = model.makeFieldSet(newTable)
        val join = Join(
            table = newTable,
            on = on(fs),
            kind = JoinKind.OUTER
        )
        joins.add(join)
        return fs
    }

    inline fun orderBy(
        ascending: Boolean = true,
        nullsFirst: Boolean = false,
        expression: DBExpression<*>
    ) {
        orderBy.clear()
        orderBy.add(Sort(expression = expression, ascending = ascending, nullsFirst = nullsFirst))
    }

    inline fun orderByDescending(
        nullsFirst: Boolean = false,
        expression: DBExpression<*>
    ) {
        orderBy.clear()
        orderBy.add(Sort(expression = expression, ascending = false, nullsFirst = nullsFirst))
    }

    inline fun thenOrderBy(
        ascending: Boolean = true,
        nullsFirst: Boolean = false,
        expression: DBExpression<*>
    ) {
        orderBy += Sort(expression = expression, ascending = ascending, nullsFirst = nullsFirst)
    }

    inline fun thenOrderByDescending(
        nullsFirst: Boolean = false,
        expression: DBExpression<*>
    ) {
        orderBy += Sort(expression = expression, ascending = false, nullsFirst = nullsFirst)
    }

//    inline infix fun <reified K: Any, V, KEY> DBExpression<ForeignKey<KEY, K>>.has(field: KProperty1<K, V>): DBExpression<V?> {
//        joins.add(Join(
//            table = K::class.dbInfo,
//            on = db { this@has.raw equal K::class.dbInfo.primaryKey as DBExpression<KEY> },
//            kind = JoinKind.LEFT
//        ))
//        return DBExpressionBuilder.prop(field)
//    }
//    inline infix fun <reified K: Any, V, KEY> KProperty1<*, ForeignKey<KEY, K>>.has(field: KProperty1<K, V>): DBExpression<V?> {
//        joins.add(Join(
//            table = K::class.dbInfo,
//            on = db { prop(this@has).raw equal K::class.dbInfo.primaryKey as DBExpression<KEY> },
//            kind = JoinKind.LEFT
//        ))
//        return DBExpressionBuilder.prop(field)
//    }

    inline fun where(expression: DBExpression<Boolean>) {
        when (val current = where) {
            null -> {
                this.where = expression
            }
            is DBExpressionAll -> {
                this.where = DBExpressionAll(listOf(expression) + current.parts)
            }
            else -> {
                this.where = DBExpressionAll(listOf(current, expression))
            }
        }
    }
}