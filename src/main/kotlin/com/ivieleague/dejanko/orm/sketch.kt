package com.ivieleague.dejanko.orm2

interface FieldSet<T>
interface QuerySet<T, FIELDSET: FieldSet<T>>
interface OrderedQuerySet<T, FIELDSET: FieldSet<T>> : QuerySet<T, FIELDSET>
interface QueryExpression<T>
interface QueryRelation<T, FS: FieldSet<V>>

fun <T, FS: FieldSet<T>> QuerySet<T, FS>.filter(predicate: (it: FS)->QueryExpression<Boolean>): QuerySet<T, FS> = TODO()
fun <T, FS: FieldSet<T>, R, RFS: FieldSet<R>> QuerySet<T, FS>.map(predicate: (it: FS)->QueryExpression<R>): QuerySet<R, RFS> = TODO()
fun <T, FS: FieldSet<T>, R, RFS: FieldSet<R>> QuerySet<T, FS>.flatMap(predicate: (it: FS)->QueryRelation<R, RFS>): QuerySet<R, RFS> = TODO()
fun <T, FS: FieldSet<T>> QuerySet<T, FS>.sortedBy(expression: (it: FS)->QueryExpression<*>): OrderedQuerySet<T, FS> = TODO()
fun <T, FS: FieldSet<T>> OrderedQuerySet<T, FS>.take(count: Int): OrderedQuerySet<T, FS> = TODO()
fun <T, FS: FieldSet<T>> OrderedQuerySet<T, FS>.limit(count: Int): OrderedQuerySet<T, FS> = TODO()
fun <T, FS: FieldSet<T>> QuerySet<T, FS>.asSequence(): Sequence<T> = TODO()

fun <T, FS: FieldSet<T>> QueryRelation<T, FS>.resolved(): FS = TODO()

class SomeModel(
    val a: Int,
    val isPurple: Boolean,
    val testRelation: Long
) {
    companion object: FieldSet<SomeModel> {
        val a = object: QueryExpression<Int>{}
        val isPurple = object: QueryExpression<Boolean>{}
        val testRelation = object: QueryRelation<OtherModel, OtherModel.Companion>{}
    }
}

class OtherModel(
    val b: Int,
    val isGreen: Boolean
) {
    companion object: FieldSet<SomeModel> {
        val b = object: QueryExpression<Int>{}
        val isGreen = object: QueryExpression<Boolean>{}
    }
}

fun test(qs: QuerySet<SomeModel, SomeModel.Companion>) {
    qs
        .filter { it.isPurple }
        .sortedBy { it.a }
        .take(10)
        .limit(10)
        .asSequence()

    qs
        .filter { it.isPurple }
        .flatMap { it.testRelation }
        .filter { it.isGreen }
        .sortedBy { it.b }
        .take(10)
        .limit(10)
        .asSequence()
        .filter { it.b > 1 }

    qs
        .filter { it.testRelation.resolved().isGreen }

//    qs
//        .flatMap { object: QuerySet<OtherModel, OtherModel.Companion> {}. }


}