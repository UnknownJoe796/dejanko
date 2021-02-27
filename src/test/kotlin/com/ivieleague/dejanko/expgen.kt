package com.ivieleague.dejanko

import org.junit.Test

class Expgen {
    
    data class Wrap(
        val name: String,
        val type: (String) -> String,
        val wrap: (String) -> String
    ) {
        fun jvmName(text: String) = "@JvmName(\"$text$name\")"
    }

    fun Pair<Wrap, Wrap>.jvmName(text: String) = "@JvmName(\"$text${first.name}${second.name}\")"
    
    @Test
    fun run() {
        val wraps = listOf<Wrap>(
            Wrap(name = "DBExpr", type = { "DatabaseExpression<$it>" }, wrap = { it }),
            Wrap(name = "Prop", type = { "KProperty1<*, $it>" }, wrap = { "prop($it)" })
        )
        val allWraps = listOf<Wrap>(
            Wrap(name = "Direct", type = { it }, wrap = { "Constant($it)" })
        ) + wraps
        val combinations =
            allWraps.flatMap { a -> allWraps.map { b -> a to b } }.drop(1)

        println("Combos are")
        for(combo in combinations){
            println("${combo.first.name} and ${combo.second.name}")
        }

        for ((kot, const) in mapOf(
            "plus" to "MathematicalType.PLUS",
            "minus" to "MathematicalType.MINUS",
            "times" to "MathematicalType.TIMES",
            "div" to "MathematicalType.DIV"
        )) {
            for (combination in combinations) {
                println("${combination.jvmName(kot)} operator fun <T: Number> ${combination.first.type("T")}.$kot(other: ${combination.second.type("T")}): DatabaseExpression<T> = DBExpressionMathematical(${combination.first.wrap("this")}, $const, ${combination.second.wrap("other")})")
            }
        }
        for ((kot, const) in mapOf(
            "modulo" to "MathematicalType.MODULO",
            "exponent" to "MathematicalType.EXPONENT",
            "xor" to "MathematicalType.BIT_XOR",
            "shl" to "MathematicalType.BIT_SHIFT_LEFT",
            "shr" to "MathematicalType.BIT_SHIFT_RIGHT",
        )) {
            for (combination in combinations) {
                println("${combination.jvmName(kot)} infix fun <T> ${combination.first.type("T")}.$kot(other: ${combination.second.type("T")}): DatabaseExpression<T> = DBExpressionMathematical(${combination.first.wrap("this")}, $const, ${combination.second.wrap("other")})")
            }
        }
        for ((kot, const) in mapOf(
            "and" to "MathematicalType.BIT_AND",
            "or" to "MathematicalType.BIT_OR",
        )) {
            for (combination in combinations) {
                println("${combination.jvmName("bit" + kot.capitalize())} infix fun <T> ${combination.first.type("T")}.$kot(other: ${combination.second.type("T")}): DatabaseExpression<T> = DBExpressionMathematical(${combination.first.wrap("this")}, $const, ${combination.second.wrap("other")})")
            }
        }
        for (wrap in wraps) {
            println("${wrap.jvmName("unaryMinus")} operator fun <T: Number> ${wrap.type("T")}.unaryMinus(): DatabaseExpression<T> = DBExpressionMathematicalUnary(${wrap.wrap("this")}, MathematicalUnaryType.NEGATIVE)")
        }
        for((key, value) in mapOf(
            "absoluteValue" to "ABSOLUTE_VALUE",
            "squareRoot" to "SQUARE_ROOT",
            "cubeRoot" to "CUBE_ROOT",
        )) {
            for (wrap in wraps) {
                println("${wrap.jvmName(key)} fun <T: Number> ${wrap.type("T")}.$key(): DatabaseExpression<T> = DBExpressionMathematicalUnary(${wrap.wrap("this")}, MathematicalUnaryType.$value)")
            }
        }
        for ((kot, const) in mapOf(
            "equal" to "ComparisonType.EQUAL",
            "notEqual" to "ComparisonType.NOT_EQUAL",
            "lessThan" to "ComparisonType.LESS_THAN",
            "lessThanEqual" to "ComparisonType.LESS_THAN_EQUAL",
            "greaterThan" to "ComparisonType.GREATER_THAN",
            "greaterThanEqual" to "ComparisonType.GREATER_THAN_EQUAL",
        )) {
            for (combination in combinations) {
                println("${combination.jvmName(kot)} infix fun <T> ${combination.first.type("T")}.$kot(other: ${combination.second.type("T")}): DatabaseExpression<Boolean> = DBExpressionCompare(${combination.first.wrap("this")}, $const, ${combination.second.wrap("other")})")
            }
        }
        for (combination in combinations) {
            println("${combination.jvmName("and")} infix fun ${combination.first.type("Boolean")}.and(other: ${combination.second.type("Boolean")}): DatabaseExpression<Boolean> = DBExpressionAll(listOf(${combination.first.wrap("this")}, ${combination.second.wrap("other")}))")
        }
        for (combination in combinations) {
            println("${combination.jvmName("or")} infix fun ${combination.first.type("Boolean")}.or(other: ${combination.second.type("Boolean")}): DatabaseExpression<Boolean> = DBExpressionAny(listOf(${combination.first.wrap("this")}, ${combination.second.wrap("other")}))")
        }
        for (wrap in wraps) {
            println("${wrap.jvmName("not")} operator fun ${wrap.type("Boolean")}.not(): DatabaseExpression<Boolean> = DBExpressionNot(${wrap.wrap("this")})")
        }
        for (combination in combinations) {
            println("${combination.jvmName("concat")} operator fun ${combination.first.type("String")}.plus(other: ${combination.second.type("String")}): DatabaseExpression<String> = DBExpressionConcat(listOf(${combination.first.wrap("this")}, ${combination.second.wrap("other")}))")
        }
        for (wrap in wraps) {
            println("${wrap.jvmName("toStringExp")} fun <T> ${wrap.type("T")}.toStringExp(): DatabaseExpression<String> = DBExpressionCast(${wrap.wrap("this")}, DBTypeNoOp<String>())")
        }
        for ((kot, const) in mapOf(
            "like" to "LIKE",
            "matchesRegex" to "MATCHES_REGEX",
            "insensitiveMatchesRegex" to "INSENSITIVE_MATCHES_REGEX",
            "notMatchesRegex" to "NOT_MATCHES_REGEX",
            "notInsensitiveMatchesRegex" to "NOT_INSENSITIVE_MATCHES_REGEX",
        )) {
            for (combination in combinations) {
                println("${combination.jvmName(kot)} infix fun ${combination.first.type("String")}.$kot(other: ${combination.second.type("String")}): DatabaseExpression<Boolean> = DBExpressionStringComparison(${combination.first.wrap("this")}, StringComparisonType.$const, ${combination.second.wrap("other")})")
            }
        }
        for (combination in combinations) {
            println("${combination.jvmName("coalesce")} infix fun <T> ${combination.first.type("T?")}.coalesce(other: ${combination.second.type("T?")}): DatabaseExpression<T?> = DBExpressionCoalesce(listOf(${combination.first.wrap("this")}, ${combination.second.wrap("other")}))")
        }
    }
}