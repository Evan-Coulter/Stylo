package com.example.stylo.list;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.stylo.data.NotesRepository;

public class NoteListViewModelFactory implements ViewModelProvider.Factory {
    private final NotesRepository repository;

    public NoteListViewModelFactory(NotesRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new NoteListViewModel(repository);
    }
}
