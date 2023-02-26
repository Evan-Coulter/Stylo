package com.example.stylo.list

/**
 * Events from activity or fragment such as a button press which
 * view model should respond to.
 */
sealed class NoteListEvent {
    object PageLoaded : NoteListEvent()
    object HelpPushed : NoteListEvent()
    object LogoPushed : NoteListEvent()
    object FolderButtonPushed : NoteListEvent()
    object SearchButtonPushed : NoteListEvent()
    data class SearchCompleted(val query: String) : NoteListEvent()
    object CardListViewSwitchPushed : NoteListEvent()
    data class NotePushed(val noteID: Int) : NoteListEvent()
    data class DeleteNoteButtonPushed(val noteID : Int) : NoteListEvent()
    data class AddNewNoteButtonPushed(val noteID : Int) : NoteListEvent()
    data class EditNoteButtonPushed(val noteID: Int) : NoteListEvent()
    data class ChangeFolderButtonPushed(val folderID : Int) : NoteListEvent()
    data class DeleteFolderButtonPushed(val folderID : Int) : NoteListEvent()
    data class AddNewFolderButtonPushed(val folderID : Int) : NoteListEvent()
    data class EditFolderButtonPushed(val folderID : Int) : NoteListEvent()
}
