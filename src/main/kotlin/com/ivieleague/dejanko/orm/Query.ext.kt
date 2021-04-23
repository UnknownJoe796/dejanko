package com.ivieleague.dejanko.orm

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.jvmErasure


//fun <Q, T : Any> Query<Q>.leftJoin(
//    type: KClass<T>,
//    expression: DBExpression<Boolean>
//): Query<Q> {
//    val info = type.dbInfo
//    return this.copy(
//        joins = joins + Join(
//            table = info,
//            on = expression,
//            kind = JoinKind.LEFT
//        )
//    )
//}
//
//fun <Q, T : Any> Query<Q>.rightJoin(
//    type: KClass<T>,
//    expression: DBExpression<Boolean>
//): Query<Q> {
//    val info = type.dbInfo
//    return this.copy(
//        joins = joins + Join(
//            table = info,
//            on = expression,
//            kind = JoinKind.RIGHT
//        )
//    )
//}
//
//fun <Q, T : Any> Query<Q>.innerJoin(
//    type: KClass<T>,
//    expression: DBExpression<Boolean>
//): Query<Q> {
//    val info = type.dbInfo
//    return this.copy(
//        joins = joins + Join(
//            table = info,
//            on = expression,
//            kind = JoinKind.INNER
//        )
//    )
//}
//
//fun <Q, T : Any> Query<Q>.outerJoin(
//    type: KClass<T>,
//    expression: DBExpression<Boolean>
//): Query<Q> {
//    val info = type.dbInfo
//    return this.copy(
//        joins = joins + Join(
//            table = info,
//            on = expression,
//            kind = JoinKind.OUTER
//        )
//    )
//}

fun <Q> Query<Q>.orderBy(
    ascending: Boolean = true,
    nullsFirst: Boolean = false,
    expression: DBExpression<*>
) = this.copy(
    orderBy = listOf(
        Sort(
            expression = expression,
            ascending = ascending,
            nullsFirst = nullsFirst
        )
    )
)

fun <Q> Query<Q>.orderByDescending(
    nullsFirst: Boolean = false,
    expression: DBExpression<*>
) = this.copy(
    orderBy = listOf(
        Sort(
            expression = expression,
            ascending = false,
            nullsFirst = nullsFirst
        )
    )
)

fun <Q> Query<Q>.thenOrderBy(
    ascending: Boolean = true,
    nullsFirst: Boolean = false,
    expression: DBExpression<*>
) = this.copy(
    orderBy = this.orderBy + listOf(
        Sort(
            expression = expression,
            ascending = ascending,
            nullsFirst = nullsFirst
        )
    )
)

fun <Q> Query<Q>.thenOrderByDescending(
    nullsFirst: Boolean = false,
    expression: DBExpression<*>
) = this.copy(
    orderBy = this.orderBy + listOf(
        Sort(
            expression = expression,
            ascending = false,
            nullsFirst = nullsFirst
        )
    )
)

fun <Q> Query<Q>.limit(limit: Int) = copy(limit = limit)
fun <Q> Query<Q>.offset(offset: Int) = copy(offset = offset)
fun <Q> Query<Q>.distinct(distinct: Boolean) = copy(distinct = distinct)

fun <Q> Query<Q>.where(expression: DBExpression<Boolean>): Query<Q> {
    return when (val current = this.where) {
        null -> {
            this.copy(where = expression)
        }
        is DBExpressionAll -> {
            this.copy(where = DBExpressionAll(listOf(expression) + current.parts))
        }
        else -> {
            this.copy(where = DBExpressionAll(listOf(current, expression)))
        }
    }
}

//inline fun <Q, KEY, reified T : Any> Query<Q>.selectRelated(
//    property: KProperty1<Q, ForeignKey<KEY, T>?>,
//    otherKey: KProperty1<T, KEY>
//): Query<Q> {
//    val type = property.returnType.arguments[1].type!!.jvmErasure
//    val info = type.dbInfo
//    val otherParser = SimpleParser(type)
//    val previousParse = this.parse
//    return this.copy(
//        select = this.select + info.columns,
//        joins = joins + Join(
//            table = info,
//            on = db { property equal otherKey },
//            kind = JoinKind.LEFT
//        ),
//        parse = {
//            val base = previousParse(it)
//            val other = otherParser(it)
//            return@copy {
//                val item = base(it)
//                val fk = property.get(item)
//                if (fk != null) {
//                    fk.prefetched = other(it) as? T
//                }
//                item
//            }
//        }
//    )
//}
//
//inline fun <Q, KEY, reified T : Any> Query<Q>.selectRelatedNN(
//    property: KProperty1<Q, ForeignKey<KEY, T>>,
//    otherKey: KProperty1<T, KEY>
//): Query<Q> {
//    val type = property.returnType.arguments[1].type!!.jvmErasure
//    val info = type.dbInfo
//    val otherParser = SimpleParser(type)
//    val previousParse = this.parse
//    return this.copy(
//        select = this.select + info.columns,
//        joins = joins + Join(
//            table = info,
//            on = db { property equal otherKey },
//            kind = JoinKind.LEFT
//        ),
//        parse = {
//            val base = previousParse(it)
//            val other = otherParser(it)
//            return@copy {
//                val item = base(it)
//                val fk = property.get(item)
//                fk.prefetched = other(it) as? T
//                item
//            }
//        }
//    )
//}