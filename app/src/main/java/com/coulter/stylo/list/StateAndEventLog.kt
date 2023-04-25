package com.coulter.stylo.list

import androidx.annotation.VisibleForTesting
import kotlin.collections.LinkedHashMap

@VisibleForTesting
object StateAndEventLog {
    var id : Long = 0
    private const val maxSize = 5
    val states = getNewQueue()
    val events = getNewQueue()
    private fun getNewQueue(): LinkedHashMap<Long, Any?> {
        return object : LinkedHashMap<Long, Any?>() {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, Any?>?): Boolean {
                return this.size > maxSize
            }
        }
    }
}


internal fun log(state: NoteListViewState) {
    StateAndEventLog.states[StateAndEventLog.id++] = state
}

internal fun log(event: NoteListEvent) {
    StateAndEventLog.events[StateAndEventLog.id++] = event
}