package com.example.stylo.editor

import androidx.lifecycle.ViewModel
import com.example.stylo.data.NotesRepository
import com.example.stylo.data.RoomNote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

class NoteEditorViewModel(note: RoomNote, private val repository: NotesRepository) : ViewModel() {
    private val currentNote: RoomNote = note.copy()

    private var _uiState: MutableStateFlow<NoteEditorViewState> = MutableStateFlow(
        NoteEditorViewState.ShowPreviousJournalOrStartPrompt(
            currentNote.title,
            currentNote.content
        )
    )
    val uiState = _uiState.asStateFlow()

    fun onStart() {
        _uiState.value = NoteEditorViewState.ShowPreviousJournalOrStartPrompt(currentNote.title, currentNote.content)
    }

    fun onResume() {
        _uiState.value = NoteEditorViewState.ShowPreviousJournalOrStartPrompt(currentNote.title, currentNote.content)
    }

    fun onTextChanged(text: String) {
        currentNote.title = text
    }

    fun onSaveClicked(title: String) {
        _uiState.value = NoteEditorViewState.ShowSavePrompt(title)
    }

    fun onTitleClicked() {
        _uiState.value = NoteEditorViewState.ShowSetTitleState(currentNote.title)
    }

    fun onSaveFinished(title: String) {
        currentNote.title = title
        currentNote.dateLastSaved = Calendar.getInstance().time
    }

    fun onEditorClosed() {
        onSaveFinished(currentNote.title)
        repository.add(currentNote)
    }
}