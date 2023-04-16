package com.example.stylo.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.stylo.data.NotesRepository
import com.example.stylo.data.model.RoomFolder
import com.example.stylo.data.model.RoomNote
import com.example.stylo.data.model.RoomNoteBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*


class NoteEditorViewModel(
    private val currentNote: RoomNote,
    private val currentFolder: RoomFolder,
    private val repository: NotesRepository
) : ViewModel() {
    private var currentNoteBuilder = RoomNoteBuilder()
        .setTitle(currentNote.title)
        .setContent(currentNote.content)
        .setUID(currentNote.uid)
        .setFileName(currentNote.filePath)
        .setDateCreated(currentNote.dateCreated)
        .setDateLastModified(currentNote.dateLastSaved)

    private var _uiState: MutableStateFlow<NoteEditorViewState> = MutableStateFlow(
        NoteEditorViewState.ShowBasicEditorScreen(currentNote, currentFolder)
    )
    val uiState = _uiState.asStateFlow()
    var _eventListener: MutableLiveData<NoteEditorEvent> = MutableLiveData()
    val eventListener: LiveData<NoteEditorEvent> = _eventListener

    private var eventListenerObserver = Observer<NoteEditorEvent> {
        when (it) {
            is NoteEditorEvent.EditorLoaded -> intializeEditor()
            is NoteEditorEvent.EditorClosed -> onEditorClosed()
        }
    }

    init {
        eventListener.observeForever(eventListenerObserver)
    }

    private fun intializeEditor() {
        val note = repository.getNote(currentNote.uid)
        val folder = repository.getFolder(currentFolder.uid)
        _uiState.value = NoteEditorViewState.ShowBasicEditorScreen(note, folder)
    }

    private fun onEditorClosed() {
        currentNoteBuilder
            .setTitle(currentNote.title)
            .setDateLastModified(Calendar.getInstance().time)
        repository.update(currentNoteBuilder.build())
    }
}