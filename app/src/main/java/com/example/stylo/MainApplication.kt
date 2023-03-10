package com.example.stylo

import android.app.Application
import com.example.stylo.data.NotesRepository
import com.example.stylo.data.database.NotesMetaDataDatabase
import com.example.stylo.data.fileaccess.FileAccessSource
import com.example.stylo.data.model.RoomFolderBuilder
import com.example.stylo.data.model.RoomNoteBuilder

class MainApplication : Application() {
    val database: NotesMetaDataDatabase by lazy { NotesMetaDataDatabase.getInstance(this) }
    val notesDao by lazy { database.notesMetaDataDao() }
    val notesRepository by lazy { NotesRepository(notesDao, FileAccessSource(applicationContext)).also { repository ->
        if (repository.getAllNotes().isEmpty()) {
            val homeworkNoteIDs = mutableListOf<Int>()
            homeworkNoteIDs.add(repository.add(RoomNoteBuilder()
                .setTitle("Homework 1")
                .setContent("Homework 1 Content")
                .also { it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build())) }
                .build()))
            repository.add(RoomNoteBuilder()
                .setTitle("Chores")
                .setContent("Chores Content")
                .also { it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build())) }
                .build())
            homeworkNoteIDs.add(repository.add(RoomNoteBuilder()
                .setTitle("Homework 2")
                .setContent("Homework 2 Content")
                .also { it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build())) }
                .build()))
            repository.add(RoomNoteBuilder()
                .setTitle("Some other stuff")
                .setContent("some other stuff content")
                .also { it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build())) }
                .build())
            val homeworkFolderId = repository.add(
                RoomFolderBuilder()
                    .setColor("Blue")
                    .setName("Homework")
                    .build()
            )
            homeworkNoteIDs.forEach {
                repository.addNoteToFolder(
                    repository.getNote(it),
                    repository.getFolder(homeworkFolderId)
                )
            }
        }
    }}
}