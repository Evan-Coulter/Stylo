package com.example.stylo.list

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.stylo.data.NotesRepository
import com.example.stylo.data.model.RoomFolder


private const val SHARED_PREF_LIST_CARD_SWITCH = "shared_pref_list_card_switch"
private const val SHARED_PREF_FOLDER_ID = "shared_pref_folder_id"

class NoteListViewModel(private val repository: NotesRepository, private val sharedPreferences: SharedPreferences) : ViewModel() {
    private var isListView: Boolean = true
    private var folder: RoomFolder = repository.getDefaultFolder()


    private var _uiState: MutableLiveData<NoteListViewState> = MutableLiveData()
    val uiState: LiveData<NoteListViewState> = _uiState

    var _eventListener: MutableLiveData<NoteListEvent> = MutableLiveData()
    val eventListener: LiveData<NoteListEvent> = _eventListener

    private var eventListenerObserver = Observer<NoteListEvent> {
        log(it)
        when (it) {
            is NoteListEvent.PageLoaded -> displayBasicListState()
            is NoteListEvent.HelpPushed -> showHelpDialog()
            is NoteListEvent.LogoPushed -> showLogoEffect()
            is NoteListEvent.FolderButtonPushed -> showFolderTray()
            is NoteListEvent.SearchButtonPushed -> showSearchBar()
            is NoteListEvent.CardListViewSwitchPushed -> switchCardListView()
            is NoteListEvent.NotePushed -> openNoteEditor(it)
            is NoteListEvent.SearchCompleted -> displaySearchResults(it)
            is NoteListEvent.EditNoteButtonPushed -> openRenameNoteDialog(it)
            is NoteListEvent.ChangeFolderButtonPushed -> changeSelectedFolder(it)
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
        postNewState(NoteListViewState.ShowFoldersTray(folders))
    }

    private fun showSearchBar() {
        postNewState(NoteListViewState.ShowSearchBar)
    }

    private fun switchCardListView() {
        isListView = !isListView
        sharedPreferences.edit().putBoolean(SHARED_PREF_LIST_CARD_SWITCH, isListView).apply()
        val notes = repository.getAllNotes()
        postNewState(NoteListViewState.ShowBasicListState(notes, folder, isListView))
    }

    private fun openNoteEditor(notePushed: NoteListEvent.NotePushed) {
        postNewState(NoteListViewState.OpenNoteEditor(repository.getNote(notePushed.noteID)))
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

    private fun openRenameNoteDialog(clickedNote: NoteListEvent.EditNoteButtonPushed) {
        val note = repository.getNote(clickedNote.noteID)
        postNewState(NoteListViewState.ShowRenameNoteDialog(note))
    }

    private fun changeSelectedFolder(folderButtonPushed: NoteListEvent.ChangeFolderButtonPushed) {
        postNewState(NoteListViewState.LoadingState)
        folder = repository.getFolder(folderButtonPushed.folderID)
        sharedPreferences.edit().putInt(SHARED_PREF_FOLDER_ID, folder.uid).apply()
        val notesInFolder = repository.getNotesInFolder(folder.uid)
        postNewState(NoteListViewState.ShowBasicListState(notesInFolder, folder, isListView))
    }

    private fun postNewState(state: NoteListViewState) {
        log(state)
        _uiState.value = state
    }
}