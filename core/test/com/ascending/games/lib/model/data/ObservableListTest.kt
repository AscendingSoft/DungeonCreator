package com.ascending.games.lib.model.data

import org.junit.Test

import org.junit.Assert.*

class ObservableListTest {

    private val list = ObservableList(mutableListOf(1,2,3))

    @Test
    fun add() {
        list.onAdd += { index, element ->
            assertEquals(3, index)
            assertEquals(4, element)
        }
        list.add(4)
    }


    @Test
    fun addAll() {
        val callbacks = mutableListOf<Pair<Int, Int>>()
        list.onAdd += { index, element ->
            callbacks.add(Pair(index, element))
        }
        list.addAll(listOf(3, 4))
        assertEquals(listOf(Pair(3, 3), Pair(4, 4)), callbacks)
    }

    @Test
    fun clear() {
        val callbacks = mutableListOf<Int>()
        list.onRemove += { element ->
            callbacks.add(element)
        }
        list.clear()
        assertEquals(listOf(1,2,3), callbacks)
    }

    @Test
    fun remove() {
        list.onRemove += { element ->
            assertEquals(2, element)
        }
        list.remove(2)
    }

    @Test
    fun testRemoveAll() {
        val callbacks = mutableListOf<Int>()
        list.onRemove += { element ->
            callbacks.add(element)
        }
        list.removeAll(listOf(3, 4))
        assertEquals(listOf(3), callbacks)
    }

    @Test
    fun removeAt() {
        val callbacks = mutableListOf<Int>()
        list.onRemove += { element ->
            callbacks.add(element)
        }
        list.removeAt(2)
        assertEquals(listOf(3), callbacks)
    }

    @Test
    fun retainAll() {
        val callbacks = mutableListOf<Int>()
        list.onRemove += { element ->
            callbacks.add(element)
        }
        list.retainAll(listOf(1,3))
        assertEquals(listOf(2), callbacks)
    }

    @Test
    fun set() {
        list.onAdd += { index, element ->
            assertEquals(1, index)
            assertEquals(3, element)
        }
        list.onRemove += {  element ->
            assertEquals(2, element)
        }
        list[1] = 3
    }
}