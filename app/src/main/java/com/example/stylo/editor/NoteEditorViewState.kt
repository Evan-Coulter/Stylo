package com.example.stylo.editor

sealed class NoteEditorViewState {
    data class ShowPreviousJournalOrStartPrompt(val noteName: String, val noteText: String) : NoteEditorViewState()
    data class ShowSavePrompt(val newNoteName: String) : NoteEditorViewState()
    object ShowSetTitleState : NoteEditorViewState()
}
