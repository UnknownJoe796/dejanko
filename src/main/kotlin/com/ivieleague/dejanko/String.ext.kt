package com.ivieleague.dejanko


private val snakeCaseRegex = Regex("[A-Z]")
fun String.snakeCase() = snakeCaseRegex.replace(this) { "_" + it.value.toLowerCase() }