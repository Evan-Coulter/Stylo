package com.example.stylo.editor

sealed class ViewState {
    data class ShowClickHereMessage(val noteName: String, val noteText: String) : ViewState()
    object ShowFullEditorView : ViewState()
    data class ShowSavePrompt(val newNoteName: String) : ViewState()
}
