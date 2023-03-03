package com.example.stylo.list

import com.example.stylo.data.model.RoomFolder
import com.example.stylo.data.model.RoomNote

/**
 * States passed from view model to activity or fragment which should
 * update the state of that view class.
 */
sealed class NoteListViewState {
    object LoadingState : NoteListViewState()
    object ShowHelpDialog : NoteListViewState()
    object ShowLogoEffect : NoteListViewState()
    object ShowSearchBar : NoteListViewState()
    object ShowEmptySearchResult : NoteListViewState()
    data class ShowBasicListState(val notes: List<RoomNote>, val folder: RoomFolder, val isListView: Boolean) : NoteListViewState()
    data class ShowEditNoteDetailsOptions(val note: RoomNote) : NoteListViewState()
    data class ShowFoldersTray(val folders: List<RoomFolder>) : NoteListViewState()
    data class ShowEditFolderDialog(val folder: RoomFolder) : NoteListViewState()
    object ShowCreateFolderDialog : NoteListViewState()
    object ShowCreateFolderSuccessMessage : NoteListViewState()
    data class ShowCreateFolderErrorMessage(val errorMessage: String) : NoteListViewState()
    object ShowEditFolderSuccessMessage : NoteListViewState()
    data class ShowEditFolderErrorMessage(val errorMessage: String) : NoteListViewState()
    data class OpenNoteEditor(val note: RoomNote) : NoteListViewState()
}
