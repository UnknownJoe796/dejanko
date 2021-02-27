package com.ivieleague.dejanko.orm

import com.ivieleague.dejanko.forEachBetween
import kotlin.reflect.KClass


interface DBExpression<T> {
    fun write(to: QueryWriter)
}

enum class ComparisonType(val symbol: String) {
    EQUAL("="),
    NOT_EQUAL("<>"),
    LESS_THAN("<"),
    LESS_THAN_EQUAL("<="),
    GREATER_THAN(">"),
    GREATER_THAN_EQUAL(">=")
}

data class DBExpressionCompare<T>(
    val left: DBExpression<T>,
    val type: ComparisonType,
    val right: DBExpression<T>
) : DBExpression<Boolean> {
    override fun write(to: QueryWriter) {
        to.append('(')
        left.write(to)
        to.append(' ')
        to.append(type.symbol)
        to.append(' ')
        right.write(to)
        to.append(')')
    }
}

data class DBExpressionIsNull<T>(
    val expression: DBExpression<T>
) : DBExpression<Boolean> {
    override fun write(to: QueryWriter) {
        to.append('(')
        expression.write(to)
        to.append(" IS NULL)")
    }
}

data class DBExpressionIsNotNull<T>(
    val expression: DBExpression<T>
) : DBExpression<Boolean> {
    override fun write(to: QueryWriter) {
        to.append('(')
        expression.write(to)
        to.append(" IS NOT NULL)")
    }
}

data class DBExpressionConstant<T>(val value: T) : DBExpression<T> {
    override fun write(to: QueryWriter) {
        to.emitVariable(value)
    }
}

enum class MathematicalType(val symbol: String) {
    PLUS("+"),
    MINUS("-"),
    TIMES("*"),
    DIV("/"),
    MODULO("%"),
    EXPONENT("^"),
    BIT_AND("&"),
    BIT_OR("|"),
    BIT_XOR("#"),
    BIT_SHIFT_LEFT("<<"),
    BIT_SHIFT_RIGHT(">>")
}

data class DBExpressionMathematical<T>(
    val left: DBExpression<T>,
    val type: MathematicalType,
    val right: DBExpression<T>
) : DBExpression<T> {
    override fun write(to: QueryWriter) {
        to.append('(')
        left.write(to)
        to.append(' ')
        to.append(type.symbol)
        to.append(' ')
        right.write(to)
        to.append(')')
    }
}

enum class MathematicalUnaryType(val symbol: String) {
    NEGATIVE("-"),
    ABSOLUTE_VALUE("@"),
    SQUARE_ROOT("|/"),
    CUBE_ROOT("||/")
}

data class DBExpressionMathematicalUnary<T>(
    val expression: DBExpression<T>,
    val type: MathematicalUnaryType
) : DBExpression<T> {
    override fun write(to: QueryWriter) {
        to.append(type.symbol)
        to.append('(')
        expression.write(to)
        to.append(')')
    }
}

data class DBExpressionAll(
    val parts: List<DBExpression<Boolean>>
) : DBExpression<Boolean> {
    override fun write(to: QueryWriter) {
        to.append('(')
        parts.forEachBetween(
            action = { it.write(to) },
            between = { to.append(" AND ") }
        )
        to.append(')')
    }
}

data class DBExpressionAny(
    val parts: List<DBExpression<Boolean>>
) : DBExpression<Boolean> {
    override fun write(to: QueryWriter) {
        to.append('(')
        parts.forEachBetween(
            action = { it.write(to) },
            between = { to.append(" OR ") }
        )
        to.append(')')
    }
}

data class DBExpressionNot(
    val expression: DBExpression<Boolean>
) : DBExpression<Boolean> {
    override fun write(to: QueryWriter) {
        to.append("NOT (")
        expression.write(to)
        to.append(')')
    }
}

enum class StringComparisonType(val symbol: String) {
    LIKE("LIKE"),
    MATCHES_REGEX("~"),
    INSENSITIVE_MATCHES_REGEX("~*"),
    NOT_MATCHES_REGEX("!~"),
    NOT_INSENSITIVE_MATCHES_REGEX("!~*")
}

data class DBExpressionStringComparison(
    val left: DBExpression<String>,
    val type: StringComparisonType,
    val right: DBExpression<String>
) : DBExpression<Boolean> {
    override fun write(to: QueryWriter) {
        to.append('(')
        left.write(to)
        to.append(' ')
        to.append(type.symbol)
        to.append(' ')
        right.write(to)
        to.append(')')
    }
}

data class DBExpressionConcat(
    val expressions: List<DBExpression<String>>
) : DBExpression<String> {
    override fun write(to: QueryWriter) {
        to.append('(')
        expressions.forEachBetween(
            action = { it.write(to) },
            between = { to.append(" || ") }
        )
        to.append(')')
    }
}

data class DBExpressionCast<A, B: Any>(
    val expression: DBExpression<A>,
    val type: DBType<B>
) : DBExpression<B> {
    override fun write(to: QueryWriter) {
        to.append("CAST (")
        expression.write(to)
        to.append(" as ")
        to.emitType(type)
        to.append(')')
    }
}

data class DBExpressionCoalesce<T>(
    val expressions: List<DBExpression<T?>>
) : DBExpression<T?> {
    override fun write(to: QueryWriter) {
        to.append("COALESCE(")
        expressions.forEachBetween(
            action = { it.write(to) },
            between = { to.append(", ") }
        )
        to.append(')')
    }
}