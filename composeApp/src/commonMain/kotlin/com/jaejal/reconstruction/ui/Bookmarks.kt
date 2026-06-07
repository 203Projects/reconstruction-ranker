package com.jaejal.reconstruction.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * A saved 평형 (unit type). Identity is (districtName, typeLabel) — the same key
 * pair the sim-history de-dupes on, since [com.jaejal.reconstruction.data.TypeInfo]
 * has no stable id of its own.
 */
data class Bookmark(
    val districtName: String,
    val typeLabel: String,
    val savedAtMs: Long
)

/**
 * In-memory bookmark store. Most-recent-first. Kept Compose-observable via a
 * SnapshotStateList but with no other Compose dependency, so the toggle/contains
 * logic is unit-testable on the JVM without a Compose runtime.
 *
 * In-memory only (matches [AppState.simHistory]); not persisted across restarts.
 */
class BookmarkStore(private val clock: () -> Long = { 0L }) {

    val items: SnapshotStateList<Bookmark> = mutableStateListOf()

    private fun indexOf(districtName: String, typeLabel: String): Int =
        items.indexOfFirst { it.districtName == districtName && it.typeLabel == typeLabel }

    fun isBookmarked(districtName: String, typeLabel: String): Boolean =
        indexOf(districtName, typeLabel) >= 0

    /** Adds if absent, removes if present. Returns the new bookmarked state. */
    fun toggle(districtName: String, typeLabel: String): Boolean {
        val idx = indexOf(districtName, typeLabel)
        return if (idx >= 0) {
            items.removeAt(idx)
            false
        } else {
            items.add(0, Bookmark(districtName, typeLabel, clock()))
            true
        }
    }

    val count: Int get() = items.size
}
