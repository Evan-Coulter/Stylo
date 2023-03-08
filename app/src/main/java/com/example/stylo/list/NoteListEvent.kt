package com.example.stylo.list

import com.example.stylo.data.model.RoomFolder

/**
 * Events from activity or fragment such as a button press which
 * view model should respond to.
 */
sealed class NoteListEvent {
    object PageLoaded : NoteListEvent() //Informs view model that the page is finished loading views.
    object HelpButtonClicked : NoteListEvent()
    object LogoButtonClicked : NoteListEvent()
    object CardListViewSwitchClicked : NoteListEvent()
    data class NoteClicked(val noteID: Int) : NoteListEvent() //Opens note editor fragment.


    object FolderTrayButtonClicked : NoteListEvent() //To enter folder tray
    data class ChangeFolderButtonClicked(val folderID : Int) : NoteListEvent() //Is used within folder tray.
    data class EditFolderButtonClicked(val folderID : Int) : NoteListEvent() //Is used within folder tray to open edit folder dialog.
    data class DeleteFolderButtonClicked(val folderID : Int) : NoteListEvent() //Is used within edit folder dialog.
    data class AttemptToEditFolder(val folder: RoomFolder) : NoteListEvent() //Is used within edit folder dialog
    object AddNewFolderButtonClicked : NoteListEvent() //Is used within folder tray to open create folder dialog.
    data class AttemptToAddNewFolder(val folder: RoomFolder) : NoteListEvent() //Is used within create folder dialog.


    object SearchButtonClicked : NoteListEvent() //To open search bar.
    object SearchCanceled : NoteListEvent() //Is used while search bar is open
    data class SearchCompleted(val query: String) : NoteListEvent() //Is used to close search bar.

    data class EditNoteButtonClicked(val noteID: Int) : NoteListEvent() //Opens note editor dialog
    data class DeleteNoteButtonClicked(val noteID : Int) : NoteListEvent() //Is used within edit note dialog
    data class ChangeNoteFolderMembershipButtonClicked(val noteID: Int, val newFolderMembership: List<Int>) : NoteListEvent()
    object AddNewNoteButtonClicked : NoteListEvent() //Opens create new note dialog.
}
