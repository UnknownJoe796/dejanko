package com.ivieleague.dejanko.orm

import com.github.jasync.sql.db.ResultSet
import com.github.jasync.sql.db.RowData
import com.github.jasync.sql.db.SuspendingConnection
import kotlin.reflect.KClass

inline fun <reified T: Any> query(
    builder: QueryBuilder.()->Unit = {}
): Query<T> {
    return QueryBuilder(T::class.dbInfo).apply {
        select<T>()
    }.apply(builder).build(SimpleParser<T>(T::class))
}