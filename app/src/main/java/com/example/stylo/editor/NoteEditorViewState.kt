package com.example.stylo.editor

import com.example.stylo.data.model.RoomFolder
import com.example.stylo.data.model.RoomNote

sealed class NoteEditorViewState {
    data class ShowBasicEditorScreen(val note: RoomNote, val folder: RoomFolder) : NoteEditorViewState()
    data class ShowNoteUpdatedState(val note: RoomNote) : NoteEditorViewState()
    data class ShowSetTitleState(val newTitle: String) : NoteEditorViewState()
}
