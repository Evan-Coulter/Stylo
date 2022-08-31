package com.example.stylo.editor

sealed class NoteEditorViewState {
    data class ShowClickHereMessage(val noteName: String, val noteText: String) : NoteEditorViewState()
    object ShowFullEditorView : NoteEditorViewState()
    data class ShowSavePrompt(val newNoteName: String) : NoteEditorViewState()
}
