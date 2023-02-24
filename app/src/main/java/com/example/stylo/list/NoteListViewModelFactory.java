package com.example.stylo.list;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.stylo.R;
import com.example.stylo.data.NotesRepository;

public class NoteListViewModelFactory implements ViewModelProvider.Factory {
    private final NotesRepository repository;
    private final Application application;

    public NoteListViewModelFactory(NotesRepository repository, Application applicationContext) {
        this.repository = repository;
        this.application = applicationContext;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        SharedPreferences sharedPreferences = application.getSharedPreferences(
            application.getString(R.string.com_example_stylo_PREFERENCE_FILE_KEY),
            Context.MODE_PRIVATE
        );
        return (T) new NoteListViewModel(repository, sharedPreferences);
    }
}
