package com.example.stylo.editor

sealed class NoteEditorEvent {
    object EditorLoaded : NoteEditorEvent()
    object EditorClosed : NoteEditorEvent()
}