package com.example.stylo

import android.app.Application
import com.example.stylo.data.database.NotesMetaDataDatabase
import com.example.stylo.data.NotesRepository
import com.example.stylo.data.fileaccess.FileAccessSource

class MainApplication : Application() {
    val database: NotesMetaDataDatabase by lazy { NotesMetaDataDatabase.getInstance(this) }
    val notesDao by lazy { database.notesMetaDataDao() }
    val notesRepository by lazy { NotesRepository(notesDao, FileAccessSource(applicationContext)) }
}