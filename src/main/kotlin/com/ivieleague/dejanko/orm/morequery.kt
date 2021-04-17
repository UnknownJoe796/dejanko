package com.ivieleague.dejanko.orm

import kotlin.reflect.KClass

inline fun <T: Any> KClass<T>.query(setup: QueryBuilder.()->Unit) = QueryBuilder(this.dbInfo).apply(setup).build()