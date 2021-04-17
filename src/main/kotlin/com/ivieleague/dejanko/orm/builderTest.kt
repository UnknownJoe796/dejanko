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

    val startA = System.currentTimeMillis()
    println(TestModel::class.dbInfo)
    println(LinkedModel::class.dbInfo)
    println(System.currentTimeMillis() - startA)

    val queryA = with(QueryBuilder(TestModel::class.dbInfo)) {
        leftJoin<LinkedModel> { LinkedModel::testModel equal TestModel::id }
//        val total = select("plusOne") { LinkedModel::x + 1 }
        where { TestModel::created greaterThan Date() }
        select<TestModel>()
        build()
    }

    val queryB = with(QueryBuilder(LinkedModel::class.dbInfo)) {
        select<LinkedModel>()
        where { LinkedModel::testModel has TestModel::k greaterThan 4 }
        build()
    }

    println(QueryWriter().also { queryA.write(it) }.builder.toString())
    println(QueryWriter().also { queryB.write(it) }.builder.toString())

    val start = System.currentTimeMillis()
    repeat(10000){
        with(QueryBuilder(LinkedModel::class.dbInfo)) {
            select<LinkedModel>()
            where { LinkedModel::testModel has TestModel::k greaterThan 4 }
            build()
        }
    }
    println(System.currentTimeMillis() - start)
}