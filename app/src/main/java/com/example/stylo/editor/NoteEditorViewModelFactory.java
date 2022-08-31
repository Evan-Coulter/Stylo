package com.example.stylo.editor;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.stylo.data.NotesRepository;
import com.example.stylo.data.RoomNote;

public class NoteEditorViewModelFactory implements ViewModelProvider.Factory {
    private RoomNote note;
    private NotesRepository repository;

    public NoteEditorViewModelFactory(RoomNote note, NotesRepository repository) {
        this.note = note;
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new NoteEditorViewModel(note, repository);
    }
}
