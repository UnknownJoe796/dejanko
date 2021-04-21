package com.ivieleague.dejanko.orm

import com.github.jasync.sql.db.ResultSet
import com.github.jasync.sql.db.RowData
import com.github.jasync.sql.db.SuspendingConnection
import com.ivieleague.dejanko.forEachBetween

data class Query<T>(
    val select: List<DBColumn<*>>,
    val from: DBInfo<*>,
    val joins: List<Join> = listOf(),
    val where: DBExpression<Boolean>? = null,
    val orderBy: List<Sort> = listOf(),
    val limit: Int? = null,
    val offset: Int? = null,
    val distinct: Boolean = false,
    val groupBy: DBExpression<*>? = null,
    val parse: (ResultSet) -> TypedResultSet<T>
) {
    fun write(to: QueryWriter){
        to.append("SELECT ")
        if(distinct){
            to.append("DISTINCT ")
        }
        select.forEachBetween(
            action = { it.writeSelect(to) },
            between = { to.append(", ") }
        )
        to.append(" FROM ")
        to.append(from.tableName)
        to.append(' ')
        joins.forEach { it.write(to) }
        if(where != null){
            to.append("WHERE ")
            where.write(to)
            to.append(' ')
        }
        if(groupBy != null){
            to.append("GROUP BY ")
            groupBy.write(to)
        }
        if(orderBy.isNotEmpty()){
            to.append("ORDER BY ")
            orderBy.forEachBetween(
                action = { it.write(to) },
                between = { to.append(", ") }
            )
            to.append(' ')
        }
        if(limit != null){
            to.append("LIMIT $limit ")
        }
        if(offset != null){
            to.append("OFFSET $offset ")
        }
    }

    override fun toString(): String {
        val writer = QueryWriter()
        this.write(writer)
        return writer.builder.toString()
    }

    suspend fun execute(db: SuspendingConnection = Settings.defaultDb): TypedResultSet<T> {
        val writer = QueryWriter()
        this.write(writer)
        return parse(db.sendPreparedStatement(writer.toString(), writer.variables).rows)
    }
}

data class Sort(
    val expression: DBExpression<*>,
    val ascending: Boolean = true,
    val nullsFirst: Boolean = false
) {
    fun write(to: QueryWriter){
        expression.write(to)
        if(ascending)
            to.append(" ASC ")
        else
            to.append(" DESC ")
        if(nullsFirst)
            to.append("NULLS FIRST")
        else
            to.append("NULLS LAST")
    }
}

enum class JoinKind(val sql: String) { LEFT("LEFT OUTER"), RIGHT("OUTER RIGHT"), INNER("INNER"), OUTER("OUTER") }

data class Join(
    val table: String,
    val on: DBExpression<Boolean>,
    val kind: JoinKind = JoinKind.INNER
) {

    fun write(to: QueryWriter){
        to.append(kind.sql)
        to.append(" JOIN ")
        to.append(table)
        to.append(" ON ")
        on.write(to)
        to.append(' ')
    }
}