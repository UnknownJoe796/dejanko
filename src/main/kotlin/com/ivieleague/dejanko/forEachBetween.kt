package com.ivieleague.dejanko

inline fun <T> Iterable<T>.forEachBetween(action: (T)->Unit, between: ()->Unit) {
    var first = true
    for (part in this) {
        if (first) {
            first = false
        } else {
            between()
        }
        action(part)
    }
}