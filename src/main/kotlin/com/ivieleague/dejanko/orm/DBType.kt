package com.ivieleague.dejanko.orm

import com.ivieleague.dejanko.snakeCase
import com.ivieleague.dejanko.type
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.jvmErasure


interface DBType<T> {
    fun parse(value: Any?): T
    fun getColumnName(defaultName: String): String = defaultName
}

@Suppress("UNCHECKED_CAST")
fun <T: Any> KClass<T>.dbType(): DBType<T> {
    return when(this) {
        ForeignKey::class -> throw IllegalArgumentException()
        MediaFile::class -> DBTypeMediaFile as DBType<T>
        else -> DBTypeNoOp()
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T> dbType() = type<T>().dbType() as DBType<T>

@Suppress("UNCHECKED_CAST")
fun KType.dbType(): DBType<*> {
    val base = when(jvmErasure) {
        ForeignKey::class -> DBTypeForeignKey(arguments[1].type!!.jvmErasure as KClass<Any>, arguments[0].type!!.dbType())
        MediaFile::class -> DBTypeMediaFile
        else -> DBTypeNoOp()
    }
    return if(this.isMarkedNullable) DBTypeNullable(base) else base
}

class DBTypeNoOp<T>: DBType<T> {
    @Suppress("UNCHECKED_CAST")
    override fun parse(value: Any?): T = value as T
}

object DBTypeMediaFile: DBType<MediaFile> {
    @Suppress("UNCHECKED_CAST")
    override fun parse(value: Any?): MediaFile = MediaFile(value as String)
}

class DBTypeForeignKey<KEY, TABLE: Any>(val table: KClass<TABLE>, val keyType: DBType<KEY>): DBType<ForeignKey<KEY, TABLE>> {
    override fun parse(value: Any?): ForeignKey<KEY, TABLE> = ForeignKey(table, keyType.parse(value))
    override fun getColumnName(defaultName: String): String {
        return defaultName + "_" + table.pkInfo.name.snakeCase()
    }
}

class DBTypeNullable<T>(val type: DBType<T>): DBType<T?> {
    override fun parse(value: Any?): T? {
        return if(value == null) null
        else type.parse(value)
    }
}