package com.coulter.stylo.list

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StateAndEventLogTest {
    @Test
    fun simpleQueueTest() {
        log(NoteListViewState.LoadingState)
        log(NoteListViewState.ShowLogoEffect)
        log(NoteListViewState.ShowSearchBar)
        log(NoteListViewState.ShowHelpDialog)
        log(NoteListViewState.ShowEmptySearchResult)

        assertEquals(5, StateAndEventLog.states.size)
        assertTrue(StateAndEventLog.states.values.last() is NoteListViewState.ShowEmptySearchResult)
        assertTrue(StateAndEventLog.states.values.first() is NoteListViewState.LoadingState)

        log(NoteListViewState.ShowSearchBar)

        assertTrue(StateAndEventLog.states.values.last() is NoteListViewState.ShowSearchBar)
        assertTrue(StateAndEventLog.states.values.first() is NoteListViewState.ShowLogoEffect)
    }
}