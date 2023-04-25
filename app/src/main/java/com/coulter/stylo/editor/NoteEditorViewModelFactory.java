package com.coulter.stylo.editor;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.coulter.stylo.data.NotesRepository;
import com.coulter.stylo.data.model.RoomFolder;
import com.coulter.stylo.data.model.RoomNote;

public class NoteEditorViewModelFactory implements ViewModelProvider.Factory {
    private final RoomNote note;
    private final RoomFolder folder;
    private final NotesRepository repository;

    public NoteEditorViewModelFactory(RoomNote note, RoomFolder folder, NotesRepository repository) {
        this.note = note;
        this.folder = folder;
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new NoteEditorViewModel(note, folder, repository);
    }
}
