package com.ivieleague.dejanko

import kotlin.reflect.KType

abstract class Generic<T>
inline fun <reified T> type(): KType {
    return (object: Generic<T>() {})::class.supertypes[0].arguments[0].type!!
}