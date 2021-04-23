package com.ivieleague.dejanko.orm

import java.util.*

data class TestModel(
    @PrimaryKey val id: Int,
    val k: Int,
    val created: Date
) {
    companion object: ModelCompanion<TestModel, Fields>(::Fields)
    class Fields(override val table: DBTable): FieldSet<TestModel> {
        val id = table.columnForProperty(TestModel::id)!!
        val k = table.columnForProperty(TestModel::k)!!
        val created = table.columnForProperty(TestModel::created)!!
    }
}
data class LinkedModel(
    @PrimaryKey val id: Int,
    val testModel: ForeignKey<Int, TestModel>,
    val x: Int
) {
    companion object: ModelCompanion<LinkedModel, Fields>(::Fields)
    class Fields(override val table: DBTable): FieldSet<LinkedModel> {
        val id = table.columnForProperty(LinkedModel::id)!!
        val testModel = table.columnForProperty(LinkedModel::testModel)!!
        val x = table.columnForProperty(LinkedModel::x)!!
    }
}

fun main(){

    println(TestModel::class.dbInfo)
    println(LinkedModel::class.dbInfo)

    val queryA = TestModel.query {
        where = it.id equal 2
    }
    val queryB = LinkedModel.query {
        // val testModel = innerJoin(TestModel)
        //
        val testModel = innerJoin(TestModel) { m -> m.id equal it.testModel.raw }
        where = testModel.k greaterThan 1
    }

    println(queryA)
    println(queryB)
}