package com.ivieleague.dejanko.orm

import com.ivieleague.dejanko.orm.DBExpressionBuilder.equal
import com.ivieleague.dejanko.orm.DBExpressionBuilder.raw
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1


fun <Q, T: Any> Query<Q>.leftJoin(type: KClass<T>, expression: DBExpressionBuilder.()->DBExpression<Boolean>): Query<Q> {
    val info = type.dbInfo
    return this.copy(joins = joins + Join(
        table = info.tableName,
        on = db(expression),
        kind = JoinKind.LEFT
    ))
}

fun <Q, T: Any> Query<Q>.rightJoin(type: KClass<T>, expression: DBExpressionBuilder.()->DBExpression<Boolean>): Query<Q> {
    val info = type.dbInfo
    return this.copy(joins = joins + Join(
        table = info.tableName,
        on = db(expression),
        kind = JoinKind.RIGHT
    ))
}

fun <Q, T: Any> Query<Q>.innerJoin(type: KClass<T>, expression: DBExpressionBuilder.()->DBExpression<Boolean>): Query<Q> {
    val info = type.dbInfo
    return this.copy(joins = joins + Join(
        table = info.tableName,
        on = db(expression),
        kind = JoinKind.INNER
    ))
}

fun <Q, T: Any> Query<Q>.outerJoin(type: KClass<T>, expression: DBExpressionBuilder.()->DBExpression<Boolean>): Query<Q> {
    val info = type.dbInfo
    return this.copy(joins = joins + Join(
        table = info.tableName,
        on = db(expression),
        kind = JoinKind.OUTER
    ))
}

inline fun <Q> Query<Q>.orderBy(
    ascending: Boolean = true,
    nullsFirst: Boolean = false,
    expression: DBExpressionBuilder.() -> DBExpression<*>
) = this.copy(orderBy = listOf(Sort(expression = with(DBExpressionBuilder, expression), ascending = ascending, nullsFirst = nullsFirst)))

inline fun <Q> Query<Q>.orderByDescending(
    nullsFirst: Boolean = false,
    expression: DBExpressionBuilder.() -> DBExpression<*>
) = this.copy(
    orderBy = listOf(Sort(expression = with(DBExpressionBuilder, expression), ascending = false, nullsFirst = nullsFirst))
)

inline fun <Q> Query<Q>.where(expression: DBExpressionBuilder.()->DBExpression<Boolean>): Query<Q> {
    return when (val current = this.where) {
        null -> {
            this.copy(where = db(expression))
        }
        is DBExpressionAll -> {
            this.copy(where = DBExpressionAll(listOf(db(expression)) + current.parts))
        }
        else -> {
            this.copy(where = DBExpressionAll(listOf(current, db(expression))))
        }
    }
}