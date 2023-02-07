package com.example.stylo.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stylo.data.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteListViewModel(private val repository: NotesRepository) : ViewModel() {
    //TODO: store in shared preferences
    private var isListView = true

    private var _uiState: MutableStateFlow<NoteListViewState> = MutableStateFlow(NoteListViewState.LoadingState)
    val uiState = _uiState.asStateFlow()

    var _eventListener: MutableStateFlow<NoteListEvent> = MutableStateFlow(NoteListEvent.PageLoaded)
    val eventListener = _eventListener.asStateFlow()

    init {
        viewModelScope.launch {
            eventListener.collect {
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
        }
    }

    private fun displayBasicListState() {
        val notes = repository.getAllNotes()
        _uiState.value = NoteListViewState.ShowBasicListState(notes, isListView)
    }

    private fun showHelpDialog() {
        _uiState.value = NoteListViewState.ShowHelpDialog
    }

    private fun showLogoEffect() {
        _uiState.value = NoteListViewState.ShowLogoEffect
    }

    private fun showFolderTray() {
        val folders = repository.getAllFolders()
        _uiState.value = NoteListViewState.ShowFoldersTray(folders)
    }

    private fun showSearchBar() {
        _uiState.value = NoteListViewState.ShowSearchBar
    }

    private fun switchCardListView() {
        isListView = !isListView
        val notes = repository.getAllNotes()
        _uiState.value = NoteListViewState.ShowBasicListState(notes, isListView)
    }

    private fun openNoteEditor(notePushed: NoteListEvent.NotePushed) {
        _uiState.value = NoteListViewState.OpenNoteEditor(repository.getNote(notePushed.noteID))
    }

    private fun displaySearchResults(searchEvent: NoteListEvent.SearchCompleted) {
        _uiState.value = NoteListViewState.LoadingState
        val notes = repository.getAllNotes().filter {
            it.title.lowercase().contains(searchEvent.query.lowercase())
            || it.content.lowercase().contains(searchEvent.query)
        }
        if (notes.isEmpty()) {
            _uiState.value = NoteListViewState.ShowEmptySearchResult
        } else {
            _uiState.value = NoteListViewState.ShowBasicListState(notes, isListView)
        }
    }

    private fun openRenameNoteDialog(clickedNote: NoteListEvent.EditNoteButtonPushed) {
        val note = repository.getNote(clickedNote.noteID)
        _uiState.value = NoteListViewState.ShowRenameNoteDialog(note)
    }
}