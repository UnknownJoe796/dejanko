package com.ivieleague.dejanko

import java.util.ArrayList
import java.util.NoSuchElementException
import java.util.function.Consumer


open class LazyMapList<A, T>(val source: List<A>, val converter: (A) -> T): List<T> {
    val converted = arrayOfNulls<Any?>(source.size)
    override val size: Int get() = source.size
    override fun contains(element: T): Boolean = this.any { it == element }
    override fun containsAll(elements: Collection<T>): Boolean = elements.all { it in this }
    override fun get(index: Int): T {
        val existing = converted[index]
        if(existing != null) return existing as T
        val generated = converter(source[index])
        converted[index] = generated
        return generated
    }

    override fun indexOf(element: T): Int = this.asSequence().indexOf(element)
    override fun isEmpty(): Boolean = source.isEmpty()
    override fun iterator(): Iterator<T> = Itr()

    override fun lastIndexOf(element: T): Int = size - this.asReversed().asSequence().indexOf(element) - 1

    override fun listIterator(): ListIterator<T> = ListItr(0)

    override fun listIterator(index: Int): ListIterator<T> = ListItr(index)

    override fun subList(fromIndex: Int, toIndex: Int): List<T> = ArrayList(this).subList(fromIndex, toIndex)

    /**
     * An optimized version of AbstractList.Itr
     */
    private open inner class Itr() : Iterator<T> {
        var cursor // index of next element to return
                = 0
        var lastRet = -1 // index of last element returned; -1 if no such
        override fun hasNext(): Boolean {
            return cursor != size
        }

        override fun next(): T {
            val i = cursor
            if (i >= size) throw NoSuchElementException()
            cursor = i + 1
            return this@LazyMapList[i.also { lastRet = it }]
        }

        override fun forEachRemaining(action: Consumer<in T>) {
            val size: Int = this@LazyMapList.size
            var i = cursor
            if (i < size) {
                while (i < size) {
                    action.accept(this@LazyMapList[i])
                    i++
                }
                // update once at end to reduce heap write traffic
                cursor = i
                lastRet = i - 1
            }
        }
    }
    private inner class ListItr(index: Int) : Itr(), ListIterator<T> {
        override fun hasPrevious(): Boolean {
            return cursor != 0
        }

        override fun nextIndex(): Int {
            return cursor
        }

        override fun previousIndex(): Int {
            return cursor - 1
        }

        override fun previous(): T {
            val i: Int = cursor - 1
            if (i < 0) throw NoSuchElementException()
            if (i >= this@LazyMapList.size) throw java.util.ConcurrentModificationException()
            cursor = i
            return this@LazyMapList[i.also { lastRet = it }]
        }

        init {
            cursor = index
        }
    }
}