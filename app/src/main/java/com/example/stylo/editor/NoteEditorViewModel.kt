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

    private var _uiState: MutableStateFlow<ViewState> = MutableStateFlow(
        ViewState.ShowClickHereMessage(
            editorTitle,
            editorText
        )
    )
    val uiState: StateFlow<ViewState>
        get() = _uiState

    fun onStart() {
        _uiState.value = ViewState.ShowClickHereMessage(editorTitle, editorText)
    }

    fun onResume() {
        _uiState.value = ViewState.ShowClickHereMessage(editorTitle, editorText)
    }

    fun onEditorClicked() {
        _uiState.value = ViewState.ShowFullEditorView
    }

    fun onTextChanged(text: String) {
        editorText = text
    }

    fun onSaveClicked(title: String) {
        _uiState.value = ViewState.ShowSavePrompt(title)
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