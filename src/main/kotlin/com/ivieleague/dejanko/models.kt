package com.ivieleague.dejanko

import kotlin.reflect.KClass


data class ForeignKey<KEY, T: Any>(val targetType: KClass<T>, val key: KEY) {
    suspend fun resolve(): T {
        return Settings.defaultDb
            .sendPreparedStatement(targetType.dbInfo.queryStart() + " WHERE id=?", listOf(key))
            .rows.parsed(targetType).single()
    }
}

data class MediaFile(val path: String)