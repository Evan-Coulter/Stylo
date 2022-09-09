package com.example.stylo.editor

import androidx.lifecycle.ViewModel
import com.example.stylo.data.NotesRepository
import com.example.stylo.data.model.DEFAULT_NEW_NOTE_FILE_PATH
import com.example.stylo.data.model.RoomNote
import com.example.stylo.data.model.RoomNoteBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*


class NoteEditorViewModel(currentNote: RoomNote, private val repository: NotesRepository) : ViewModel() {
    private var currentNoteBuilder = RoomNoteBuilder()
        .setTitle(currentNote.title)
        .setContent(currentNote.content)
        .setUID(currentNote.uid)
        .setFilePath(currentNote.filePath)
        .setDateCreated(currentNote.dateCreated)
        .setDateLastModified(currentNote.dateLastSaved)

    private var _uiState: MutableStateFlow<NoteEditorViewState> = MutableStateFlow(
        NoteEditorViewState.ShowBasicEditorScreen(
            currentNote.title,
            currentNote.content
        )
    )
    val uiState = _uiState.asStateFlow()

    fun onResume() {
        _uiState.value = NoteEditorViewState.ShowBasicEditorScreen(currentNoteBuilder.title, currentNoteBuilder.content)
    }

    fun onTextChanged(text: String) {
        currentNoteBuilder.setContent(text)
    }

    fun onSaveClicked(title: String) {
        _uiState.value = NoteEditorViewState.ShowSavePrompt(title)
    }

    fun onTitleClicked() {
        _uiState.value = NoteEditorViewState.ShowSetTitleState(currentNoteBuilder.title)
    }

    fun onSaveFinished(title: String) {
        currentNoteBuilder
            .setTitle(title)
            .setDateLastModified(Calendar.getInstance().time)
        val newFileName = if (currentNoteBuilder.filePath == DEFAULT_NEW_NOTE_FILE_PATH) {
            repository.generateNewFileName(currentNoteBuilder.build())
        } else {
            currentNoteBuilder.filePath
        }
        currentNoteBuilder.setFilePath(newFileName)
        repository.addNote(currentNoteBuilder.build())
    }

    fun onSaveCanceled() {
        _uiState.value = NoteEditorViewState.ShowBasicEditorScreen(currentNoteBuilder.title, currentNoteBuilder.content)
    }

    fun onEditorClosed() {
        onSaveFinished(currentNoteBuilder.title)
    }
}