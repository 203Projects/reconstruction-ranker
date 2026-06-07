package com.jaejal.reconstruction.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BookmarkStoreTest {

    @Test
    fun empty_store_has_nothing_bookmarked() {
        val store = BookmarkStore()
        assertFalse(store.isBookmarked("압구정3구역", "43평"))
        assertEquals(0, store.count)
    }

    @Test
    fun toggle_adds_then_removes() {
        val store = BookmarkStore()
        val on = store.toggle("압구정3구역", "43평")
        assertTrue(on, "first toggle turns bookmark on")
        assertTrue(store.isBookmarked("압구정3구역", "43평"))
        assertEquals(1, store.count)

        val off = store.toggle("압구정3구역", "43평")
        assertFalse(off, "second toggle turns bookmark off")
        assertFalse(store.isBookmarked("압구정3구역", "43평"))
        assertEquals(0, store.count)
    }

    @Test
    fun bookmarks_are_keyed_by_district_and_type() {
        val store = BookmarkStore()
        store.toggle("압구정3구역", "43평")
        // same type label in a different district is independent
        assertFalse(store.isBookmarked("신반포2차", "43평"))
        store.toggle("신반포2차", "43평")
        assertTrue(store.isBookmarked("압구정3구역", "43평"))
        assertTrue(store.isBookmarked("신반포2차", "43평"))
        assertEquals(2, store.count)
    }

    @Test
    fun newest_bookmark_is_first() {
        var t = 0L
        val store = BookmarkStore(clock = { t })
        t = 100; store.toggle("압구정3구역", "43평")
        t = 200; store.toggle("신반포2차", "59평")
        assertEquals("신반포2차", store.items.first().districtName)
        assertEquals(200L, store.items.first().savedAtMs)
        assertEquals("압구정3구역", store.items.last().districtName)
    }
}
