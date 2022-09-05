package com.example.stylo.editor

import androidx.lifecycle.ViewModel
import com.example.stylo.data.NotesRepository
import com.example.stylo.data.model.DEFAULT_NEW_NOTE_TITLE
import com.example.stylo.data.model.RoomNote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*


class NoteEditorViewModel(private val currentNote: RoomNote, private val repository: NotesRepository) : ViewModel() {
    private var currentTitle = DEFAULT_NEW_NOTE_TITLE
    private var currentContents = ""
    private val dateCreated = Calendar.getInstance().time
    private var dateLastEdited = dateCreated

    private var _uiState: MutableStateFlow<NoteEditorViewState> = MutableStateFlow(
        NoteEditorViewState.ShowBasicEditorScreen(
            currentNote.title,
            currentNote.content
        )
    )
    val uiState = _uiState.asStateFlow()

    fun onResume() {
        _uiState.value = NoteEditorViewState.ShowBasicEditorScreen(currentNote.title, currentNote.content)
    }

    fun onTextChanged(text: String) {
        currentTitle = text
    }

    fun onSaveClicked(title: String) {
        _uiState.value = NoteEditorViewState.ShowSavePrompt(title)
    }

    fun onTitleClicked() {
        _uiState.value = NoteEditorViewState.ShowSetTitleState(currentNote.title)
    }

    fun onSaveFinished(title: String) {
        currentTitle = title
        dateLastEdited = Calendar.getInstance().time
    }

    fun onEditorClosed() {
        onSaveFinished(currentNote.title)
        repository.addNote(currentNote)
    }
}