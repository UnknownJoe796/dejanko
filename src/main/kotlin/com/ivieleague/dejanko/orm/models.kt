package com.ivieleague.dejanko.orm

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.KProperty2


data class ForeignKey<KEY, T: Any>(val targetType: KClass<T>, val key: KEY) {
    var prefetched: T? = null
    suspend fun resolve(): T {
        val existing = prefetched
        if(existing != null) return existing
        val data = Settings.defaultDb
            .sendPreparedStatement(targetType.dbInfo.queryStart() + " WHERE id=?", listOf(key))
            .rows
        val parser = SimpleParser(targetType)(data)
        val result = TypedResultSet(data, parser).single()
        prefetched = result
        return result
    }
}

data class MediaFile(val path: String)
