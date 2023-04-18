package com.example.stylo.editor

sealed class NoteEditorEvent {
    object EditorLoaded : NoteEditorEvent()
    data class EditorClosed(val lastNoteContent : String, val lastNoteTitle: String) : NoteEditorEvent()
    data class NoteContentEdited(val newContent: String) : NoteEditorEvent()
    data class NoteTitleEdited(val newTitle: String) : NoteEditorEvent()
}