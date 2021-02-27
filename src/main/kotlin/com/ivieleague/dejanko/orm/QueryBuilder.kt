package com.ivieleague.dejanko.orm

import com.ivieleague.dejanko.type
import kotlin.math.exp
import kotlin.reflect.KProperty1

class QueryBuilder(val from: DBInfo<*>) {
    val select = ArrayList<DBColumn<*>>()
    val joins = ArrayList<Join>()
    var where: DBExpression<Boolean>? = null
    var orderBy: List<Sort> = listOf()
    var limit: Int? = null
    var offset: Int? = null
    var distinct: Boolean = false
    var groupBy: DBExpression<*>? = null

    fun build() = Query(
        select = select,
        from = from,
        joins = joins,
        where = where,
        orderBy = orderBy,
        limit = limit,
        offset = offset,
        distinct = distinct,
        groupBy = groupBy,
    )

    inline fun <reified T: Any> select() {
        select.addAll(T::class.dbInfo.columns)
    }

    inline fun <reified T> select(key: String, expression: DBExpressionBuilder.()->DBExpression<T>): DBExpressionColumn<T> {
        val col = DBExpressionColumn<T>(key, type = dbType<T>(), expression = db(expression))
        select.add(col)
        return col
    }

    inline fun <reified T: Any> leftJoin(expression: DBExpressionBuilder.()->DBExpression<Boolean>) {
        val info = T::class.dbInfo
        joins.add(Join(
            table = info.tableName,
            on = db(expression),
            kind = JoinKind.LEFT
        ))
    }

    inline fun <reified T: Any> rightJoin(expression: DBExpressionBuilder.()->DBExpression<Boolean>) {
        val info = T::class.dbInfo
        joins.add(Join(
            table = info.tableName,
            on = db(expression),
            kind = JoinKind.RIGHT
        ))
    }

    inline fun <reified T: Any> innerJoin(expression: DBExpressionBuilder.()->DBExpression<Boolean>) {
        val info = T::class.dbInfo
        joins.add(Join(
            table = info.tableName,
            on = db(expression),
            kind = JoinKind.INNER
        ))
    }

    inline fun <reified T: Any> outerJoin(expression: DBExpressionBuilder.()->DBExpression<Boolean>) {
        val info = T::class.dbInfo
        joins.add(Join(
            table = info.tableName,
            on = db(expression),
            kind = JoinKind.OUTER
        ))
    }

    inline fun orderBy(
        ascending: Boolean = true,
        nullsFirst: Boolean = false,
        expression: DBExpressionBuilder.() -> DBExpression<*>
    ) {
        orderBy = listOf(Sort(expression = with(DBExpressionBuilder, expression), ascending = ascending, nullsFirst = nullsFirst))
    }

    inline fun orderByDescending(
        nullsFirst: Boolean = false,
        expression: DBExpressionBuilder.() -> DBExpression<*>
    ) {
        orderBy = listOf(Sort(expression = with(DBExpressionBuilder, expression), ascending = false, nullsFirst = nullsFirst))
    }

    inline infix fun <reified K: Any, V, KEY> DBExpression<ForeignKey<KEY, K>>.has(field: KProperty1<K, V>): DBExpression<V?> {
        joins.add(Join(
            table = K::class.dbInfo.tableName,
            on = db { this@has.raw equal K::class.dbInfo.primaryKey as DBExpression<KEY> },
            kind = JoinKind.LEFT
        ))
        return DBExpressionBuilder.prop(field)
    }
    inline infix fun <reified K: Any, V, KEY> KProperty1<*, ForeignKey<KEY, K>>.has(field: KProperty1<K, V>): DBExpression<V?> {
        joins.add(Join(
            table = K::class.dbInfo.tableName,
            on = db { prop(this@has).raw equal K::class.dbInfo.primaryKey as DBExpression<KEY> },
            kind = JoinKind.LEFT
        ))
        return DBExpressionBuilder.prop(field)
    }

    inline fun where(expression: DBExpressionBuilder.()->DBExpression<Boolean>) {
        when (val current = where) {
            null -> {
                this.where = db(expression)
            }
            is DBExpressionAll -> {
                this.where = DBExpressionAll(listOf(db(expression)) + current.parts)
            }
            else -> {
                this.where = DBExpressionAll(listOf(current, db(expression)))
            }
        }
    }
}