package com.coulter.stylo.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coulter.stylo.data.NotesRepository
import com.coulter.stylo.data.model.RoomFolder
import com.coulter.stylo.data.model.RoomNote
import com.coulter.stylo.data.model.RoomNoteBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    private var noteBuilderMutex = Mutex()

    private var _uiState: MutableStateFlow<NoteEditorViewState> = MutableStateFlow(
        NoteEditorViewState.ShowBasicEditorScreen(currentNote, currentFolder)
    )
    val uiState = _uiState.asStateFlow()
    var _eventListener: MutableLiveData<NoteEditorEvent> = MutableLiveData()
    val eventListener: LiveData<NoteEditorEvent> = _eventListener

    private val titleChangedFlow = MutableStateFlow(currentNote.title)
    private val noteContentChangedFlow = MutableStateFlow(currentNote.content)

    private var eventListenerObserver = Observer<NoteEditorEvent> {
        when (it) {
            is NoteEditorEvent.EditorLoaded -> initializeEditor()
            is NoteEditorEvent.EditorClosed -> onEditorClosed(it.lastNoteContent, it.lastNoteTitle)
            is NoteEditorEvent.NoteContentEdited -> onNoteContentEdited(it.newContent)
            is NoteEditorEvent.NoteTitleEdited -> onNoteTitleEdited(it.newTitle)
        }
    }

    init {
        eventListener.observeForever(eventListenerObserver)

        viewModelScope.launch{
            titleChangedFlow.debounce(3000).collect { newTitle ->
                noteBuilderMutex.withLock {
                    currentNoteBuilder.setTitle(newTitle)
                    currentNoteBuilder.setDateLastModified(Calendar.getInstance().time)
                    val note = repository.getNote(repository.update(currentNoteBuilder.build()))
                    _uiState.value = NoteEditorViewState.ShowNoteUpdatedState(note)
                }
            }
        }

        viewModelScope.launch {
            noteContentChangedFlow.debounce(5000).collect { newContent ->
                noteBuilderMutex.withLock {
                    currentNoteBuilder.setContent(newContent)
                    currentNoteBuilder.setDateLastModified(Calendar.getInstance().time)
                    val note = repository.getNote(repository.update(currentNoteBuilder.build()))
                    _uiState.value = NoteEditorViewState.ShowNoteUpdatedState(note)
                }
            }
        }
    }


    private fun onNoteTitleEdited(newTitle: String) {
        viewModelScope.launch {
            titleChangedFlow.emit(newTitle)
        }
    }

    private fun onNoteContentEdited(newContent: String) {
        viewModelScope.launch {
            noteContentChangedFlow.emit(newContent)
        }
    }

    private fun initializeEditor() {
        val note = repository.getNote(currentNote.uid)
        val folder = repository.getFolder(currentFolder.uid)
        _uiState.value = NoteEditorViewState.ShowBasicEditorScreen(note, folder)
    }

    private fun onEditorClosed(lastNoteContent: String, lastNoteTitle: String) {
        runBlocking {
            noteBuilderMutex.withLock {
                currentNoteBuilder.setContent(lastNoteContent)
                currentNoteBuilder.setTitle(lastNoteTitle)
                repository.update(currentNoteBuilder.build())
            }
        }
    }

    override fun onCleared() {
        eventListener.removeObserver(eventListenerObserver)
        super.onCleared()
    }


}