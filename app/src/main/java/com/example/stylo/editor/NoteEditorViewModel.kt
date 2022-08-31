package com.example.stylo.editor

import androidx.lifecycle.ViewModel
import com.example.stylo.data.NotesRepository
import com.example.stylo.data.RoomNote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

class NoteEditorViewModel(note: RoomNote, private val repository: NotesRepository) : ViewModel() {
    private val currentNote: RoomNote = note.copy()
    private var editorTitle: String = currentNote.title
    private var editorText: String = ""

    private var _uiState: MutableStateFlow<NoteEditorViewState> = MutableStateFlow(
        NoteEditorViewState.ShowPreviousJournalOrStartPrompt(
            editorTitle,
            editorText
        )
    )
    val uiState: StateFlow<NoteEditorViewState>
        get() = _uiState

    fun onStart() {
        _uiState.value = NoteEditorViewState.ShowPreviousJournalOrStartPrompt(editorTitle, editorText)
    }

    fun onResume() {
        _uiState.value = NoteEditorViewState.ShowPreviousJournalOrStartPrompt(editorTitle, editorText)
    }

    fun onTextChanged(text: String) {
        editorText = text
    }

    fun onSaveClicked(title: String) {
        _uiState.value = NoteEditorViewState.ShowSavePrompt(title)
    }

    fun onTitleClicked() {
        _uiState.value = NoteEditorViewState.ShowSetTitleState
    }

    fun onSaveFinished(title: String) {
        editorTitle = title
        currentNote.title = editorTitle
        currentNote.dateLastSaved = Calendar.getInstance().time
    }

    fun onEditorClosed() {
        onSaveFinished(editorTitle)
        repository.add(currentNote, editorText)
    }
}