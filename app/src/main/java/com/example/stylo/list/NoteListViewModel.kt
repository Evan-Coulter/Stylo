package com.example.stylo.list

import androidx.lifecycle.ViewModel
import com.example.stylo.data.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NoteListViewModel(private val repository: NotesRepository) : ViewModel() {
    private var _uiState: MutableStateFlow<NoteListViewState> = MutableStateFlow(
        NoteListViewState.ShowBasicListState(repository.getAllNotes())
    )
    val uiState = _uiState.asStateFlow()
}