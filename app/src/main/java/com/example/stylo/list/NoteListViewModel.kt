package com.example.stylo.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.stylo.data.NotesRepository
import com.example.stylo.data.model.RoomFolder

class NoteListViewModel(private val repository: NotesRepository) : ViewModel() {
    //TODO: store in shared preferences and test to check value is the same when fragment goes out and back into lifecycle
    private var isListView = true
    //TODO: store in shared preferences and test to check value is the same when fragment goes out and back into lifecycle
    private val folder: RoomFolder = repository.getDefaultFolder()


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
        }

    }

    init {
        val initialState = NoteListViewState.LoadingState
        log(initialState)
        _uiState.value = initialState
        eventListener.observeForever(eventListenerObserver)
    }

    override fun onCleared() {
        eventListener.removeObserver(eventListenerObserver)
        super.onCleared()
    }

    private fun displayBasicListState() {
        val notes = repository.getAllNotes()
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

    private fun postNewState(state: NoteListViewState) {
        log(state)
        _uiState.value = state
    }
}