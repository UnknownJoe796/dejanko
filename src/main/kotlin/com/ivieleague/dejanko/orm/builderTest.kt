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

    val queryA = query<TestModel> {
        leftJoin<LinkedModel> { LinkedModel::testModel equal TestModel::id }
        where { TestModel::created greaterThan Date() }
    }
    val queryB = query<LinkedModel> {
        where { LinkedModel::testModel has TestModel::k greaterThan 4 }
    }

    println(queryA)
    println(queryB)
}