package com.example.stylo.list

import com.example.stylo.data.model.RoomFolder
import com.example.stylo.data.model.RoomNote

sealed class NoteListViewState {
    object ShowNoNotesSavedYetPrompt : NoteListViewState()
    data class ShowBasicListState(val list: List<RoomNote>) : NoteListViewState()
    data class ShowFoldersTray(val list: List<RoomNote>) : NoteListViewState()
    data class ShowRenameNoteDialog(val note: RoomNote) : NoteListViewState()
    data class ShowEditFolderNameOrColorDialog(val folder: RoomFolder) : NoteListViewState()
}
