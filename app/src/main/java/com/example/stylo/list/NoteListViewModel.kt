package com.example.stylo.list

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.stylo.data.NotesRepository
import com.example.stylo.data.exceptions.FOLDER_ALREADY_EXISTS_MESSAGE
import com.example.stylo.data.exceptions.FolderNotFoundException
import com.example.stylo.data.exceptions.FolderSavingError
import com.example.stylo.data.model.RoomFolder
import com.example.stylo.data.model.RoomFolderBuilder
import com.example.stylo.data.model.RoomNoteBuilder


private const val SHARED_PREF_LIST_CARD_SWITCH = "shared_pref_list_card_switch"
private const val SHARED_PREF_FOLDER_ID = "shared_pref_folder_id"

class NoteListViewModel(private val repository: NotesRepository, private val sharedPreferences: SharedPreferences) : ViewModel() {
    private var isListView: Boolean = true
    var folder: RoomFolder = repository.getDefaultFolder()


    private var _uiState: MutableLiveData<NoteListViewState> = MutableLiveData()
    val uiState: LiveData<NoteListViewState> = _uiState

    var _eventListener: MutableLiveData<NoteListEvent> = MutableLiveData()
    val eventListener: LiveData<NoteListEvent> = _eventListener

    private var eventListenerObserver = Observer<NoteListEvent> {
        log(it)
        when (it) {
            is NoteListEvent.PageLoaded -> displayBasicListState()
            is NoteListEvent.HelpButtonClicked -> showHelpDialog()
            is NoteListEvent.LogoButtonClicked -> showLogoEffect()
            is NoteListEvent.FolderTrayButtonClicked -> showFolderTray()
            is NoteListEvent.CardListViewSwitchClicked -> switchCardListView()
            is NoteListEvent.NoteClicked -> openNoteEditor(it)
            is NoteListEvent.SearchCompleted -> displaySearchResults(it)
            is NoteListEvent.SearchClosed -> displayBasicListState()
            is NoteListEvent.EditNoteButtonClicked -> openEditNoteDetailsOptions(it)
            is NoteListEvent.ChangeFolderButtonClicked -> changeSelectedFolder(it)
            is NoteListEvent.AddNewFolderButtonClicked -> openCreateNewFolderDialog()
            is NoteListEvent.EditFolderButtonClicked -> openEditFolderDialog(it)
            is NoteListEvent.AttemptToAddNewFolder -> attemptToSaveNewFolder(it)
            is NoteListEvent.DeleteFolderButtonClicked -> openDeleteFolderDialog(it)
            is NoteListEvent.AttemptToEditFolder -> attemptToEditFolder(it)
            is NoteListEvent.AttemptToDeleteNote -> deleteNote(it)
            is NoteListEvent.AttemptToChangeNoteFolderMembership -> changeNoteFolderMembership(it)
            is NoteListEvent.AttemptToRenameNote -> renameNote(it)
            is NoteListEvent.AddNewNoteButtonClicked -> addNewNote()
            is NoteListEvent.ChangeNoteFolderMembershipButtonClicked -> showChangeNoteFolderDialog(it)
            is NoteListEvent.DeleteNoteButtonClicked -> showDeleteNoteDialog(it)
            is NoteListEvent.RenameNoteButtonClicked -> showRenameNoteDialog(it)
            is NoteListEvent.AttemptToDeleteFolder -> attemptToDeleteFolder(it)
        }
    }

    init {
        //Setup view state and event listener
        val initialState = NoteListViewState.LoadingState
        log(initialState)
        _uiState.value = initialState
        eventListener.observeForever(eventListenerObserver)

        //Setup shared preferences to get saved settings
        isListView = sharedPreferences.getBoolean(SHARED_PREF_LIST_CARD_SWITCH, true)
        folder = try {
            repository.getFolder(sharedPreferences.getInt(SHARED_PREF_FOLDER_ID, 1))
        } catch (e: NoSuchElementException) {
            val folderID = repository.add(repository.getDefaultFolder())
            repository.getFolder(folderID)
        }
    }

    override fun onCleared() {
        eventListener.removeObserver(eventListenerObserver)
        super.onCleared()
    }

    private fun displayBasicListState() {
        val notes = if (folder.uid == 1) repository.getAllNotes() else repository.getNotesInFolder(folder.uid)
        folder = repository.getFolder(folder.uid) //updates folder object
        postNewState(NoteListViewState.ShowBasicListState(notes, folder, isListView))
    }

    private fun showHelpDialog() {
        postNewState(NoteListViewState.ShowHelpDialog)
    }

    private fun showLogoEffect() {
        postNewState(NoteListViewState.ShowLogoEffect)
    }

    private fun showFolderTray() {
        val folders = repository.getAllFolders()
        postNewState(NoteListViewState.ShowFoldersTray(folders, folder))
    }

    private fun switchCardListView() {
        postNewState(NoteListViewState.LoadingState)
        isListView = !isListView
        sharedPreferences.edit().putBoolean(SHARED_PREF_LIST_CARD_SWITCH, isListView).apply()
        displayBasicListState()
    }

    private fun openNoteEditor(notePushed: NoteListEvent.NoteClicked) {
        postNewState(NoteListViewState.OpenNoteEditor(repository.getNote(notePushed.noteID), repository.getFolder(folder.uid)))
    }

    private fun displaySearchResults(searchEvent: NoteListEvent.SearchCompleted) {
        postNewState(NoteListViewState.LoadingState)
        val notes = repository.getAllNotes().filter {
            it.title.lowercase().contains(searchEvent.query.lowercase())
            || it.content.lowercase().contains(searchEvent.query)
        }
        if (notes.isEmpty()) {
            postNewState(NoteListViewState.ShowEmptySearchResult)
        } else {
            postNewState(NoteListViewState.ShowBasicListState(notes, folder, isListView))
        }
    }

    private fun openEditNoteDetailsOptions(clickedNote: NoteListEvent.EditNoteButtonClicked) {
        val note = repository.getNote(clickedNote.noteID)
        postNewState(NoteListViewState.ShowEditNoteDetailsOptions(note))
    }

    private fun changeSelectedFolder(folderButtonPushed: NoteListEvent.ChangeFolderButtonClicked) {
        postNewState(NoteListViewState.LoadingState)
        folder = repository.getFolder(folderButtonPushed.folderID)
        sharedPreferences.edit().putInt(SHARED_PREF_FOLDER_ID, folder.uid).apply()
        val notesInFolder = repository.getNotesInFolder(folder.uid)
        postNewState(NoteListViewState.ShowBasicListState(notesInFolder, folder, isListView))
    }

    private fun openCreateNewFolderDialog() {
        postNewState(NoteListViewState.ShowCreateFolderDialog)
    }

    private fun attemptToSaveNewFolder(attemptToAddNewFolder: NoteListEvent.AttemptToAddNewFolder) {
        postNewState(NoteListViewState.LoadingState)
        try {
            val alreadySavedFolders = repository.getAllFolders()
            if (attemptToAddNewFolder.folder.name in alreadySavedFolders.map { it.name }) {
                throw FolderSavingError(FOLDER_ALREADY_EXISTS_MESSAGE)
            }
            repository.add(attemptToAddNewFolder.folder)
            postNewState(NoteListViewState.ShowCreateFolderSuccessMessage)
        } catch (e: FolderSavingError) {
            postNewState(NoteListViewState.ShowCreateFolderErrorMessage(e.errorMessage))
        }
    }

    private fun openEditFolderDialog(event: NoteListEvent.EditFolderButtonClicked) {
        try {
            val folder = repository.getFolder(event.folderID)
            postNewState(NoteListViewState.ShowEditFolderDialog(folder))
        } catch (e : FolderNotFoundException) {
            displayBasicListState()
        } catch (e : Throwable) {
            displayBasicListState()
        }
    }

    private fun openDeleteFolderDialog(event: NoteListEvent.DeleteFolderButtonClicked) {
        try {
            val folder = repository.getFolder(event.folderID)
            postNewState(NoteListViewState.OpenDeleteFolderDialog(folder))
        } catch (e : FolderNotFoundException) {
            displayBasicListState()
        } catch (e : Throwable) {
            displayBasicListState()
        }
    }

    private fun attemptToDeleteFolder(event: NoteListEvent.AttemptToDeleteFolder) {
        postNewState(NoteListViewState.LoadingState)
        try {
            repository.delete(repository.getFolder(event.folderID))
            if (event.folderID == this.folder.uid) {
                this.folder = repository.getFolder(1)
                sharedPreferences.edit().putInt(SHARED_PREF_FOLDER_ID, folder.uid).apply()
            }
            postNewState(NoteListViewState.ShowEditNoteDetailsSuccessMessage)
        } catch (e : Throwable) {
            displayBasicListState()
        }
    }

    private fun attemptToEditFolder(event: NoteListEvent.AttemptToEditFolder) {
        postNewState(NoteListViewState.LoadingState)
        try {
            if (event.folder.uid == 0) {
                //We technically shouldn't ever reach here if fragment layout is done properly.
                showFolderTray()
                return
            }
            val anotherFolderAlreadyHasThatTitle = repository.getAllFolders()
                .filter { it.uid != event.folder.uid }
                .map { it.name }
                .contains(event.folder.name)
            if (anotherFolderAlreadyHasThatTitle) {
                throw FolderSavingError(FOLDER_ALREADY_EXISTS_MESSAGE)
            }
            repository.update(event.folder)
            postNewState(NoteListViewState.ShowEditFolderSuccessMessage)
        } catch (e: FolderSavingError) {
            postNewState(NoteListViewState.ShowEditFolderErrorMessage(e.errorMessage))
        }
    }

    private fun deleteNote(event: NoteListEvent.AttemptToDeleteNote) {
        postNewState(NoteListViewState.LoadingState)
        repository.delete(repository.getNote(event.noteID))
        postNewState(NoteListViewState.ShowEditNoteDetailsSuccessMessage)
    }

    private fun changeNoteFolderMembership(event: NoteListEvent.AttemptToChangeNoteFolderMembership) {
        postNewState(NoteListViewState.LoadingState)
        val note = repository.getNote(event.noteID)
        //Get all folders that this note is in (other than default folder)
        val folders = repository.getAllFolders().filter {
            repository.getNotesInFolder(it.uid).map {
                roomNote ->  roomNote.uid
            }.contains(note.uid) && it.uid != 1
        }
        //Remove the note from all those folders
        folders.forEach {
            repository.deleteNoteFromFolder(note, it)
        }
        //Add the note to all valid folders that have remain in the event.folders list.
        val foldersToAdd = event.newFolderMembership.map { repository.getFolder(it) }
        foldersToAdd.forEach {
            repository.addNoteToFolder(note, it)
        }
        postNewState(NoteListViewState.ShowEditNoteDetailsSuccessMessage)
    }

    private fun renameNote(event: NoteListEvent.AttemptToRenameNote) {
        postNewState(NoteListViewState.LoadingState)
        try {
            repository.update(event.note)
            postNewState(NoteListViewState.ShowEditNoteDetailsSuccessMessage)
        } catch (error: Throwable) {
            postNewState(NoteListViewState.ShowRenameNoteErrorMessage("Sorry that title is not valid"))
        }
    }

    private fun addNewNote() {
        postNewState(NoteListViewState.LoadingState)
        val noteID = repository.add(RoomNoteBuilder().setTitle("New Note").setContent("").also {
            it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build()))
        }.build())
        val note = repository.getNote(noteID)
        repository.addNoteToFolder(note, repository.getFolder(1))
        repository.addNoteToFolder(note, folder)
        postNewState(NoteListViewState.OpenNoteEditor(note, folder))
    }

    private fun showRenameNoteDialog(event: NoteListEvent.RenameNoteButtonClicked) {
        val note = repository.getNote(event.noteID)
        postNewState(NoteListViewState.ShowRenameNoteDialog(note))
    }

    private fun showDeleteNoteDialog(event: NoteListEvent.DeleteNoteButtonClicked) {
        val note = repository.getNote(event.noteID)
        postNewState(NoteListViewState.ShowDeleteNoteDialog(note))
    }

    private fun showChangeNoteFolderDialog(event: NoteListEvent.ChangeNoteFolderMembershipButtonClicked) {
        val note = repository.getNote(event.noteID)
        val noteCurrentFolders = repository.getAllFolders().filter { f ->
            note.uid in repository.getNotesInFolder(f.uid).map { n -> n.uid }
        }
        val allFolders = repository.getAllFolders().filter { it.uid != 0 && it.uid != 1 }
        postNewState(
            NoteListViewState.ShowChangeNoteFolderMembershipDialog(
                note,
                noteCurrentFolders,
                allFolders
            )
        )
    }


    private fun postNewState(state: NoteListViewState) {
        log(state)
        _uiState.value = state
    }
}