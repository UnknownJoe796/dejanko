package com.ivieleague.dejanko.orm

import java.util.*

data class TestModel(
    @PrimaryKey val id: Int,
    val k: Int,
    val created: Date
)
data class LinkedModel(
    @PrimaryKey val id: Int,
    val testModel: ForeignKey<Int, TestModel>,
    val x: Int
)

fun main(){

    println(TestModel::class.dbInfo)
    println(LinkedModel::class.dbInfo)

    val queryA = with(QueryBuilder(TestModel::class.dbInfo)) {
        select<TestModel>()
        leftJoin<LinkedModel> { LinkedModel::testModel equal TestModel::id }
//        val total = select("plusOne") { LinkedModel::x + 1 }
        where { TestModel::created greaterThan Date() }
        build()
    }
    val queryB = with(QueryBuilder(LinkedModel::class.dbInfo)) {
        select<LinkedModel>()
        where { LinkedModel::testModel has TestModel::k greaterThan 4 }
        build()
    }

    println(QueryWriter().also { queryA.write(it) }.builder.toString())
    println(QueryWriter().also { queryB.write(it) }.builder.toString())
}