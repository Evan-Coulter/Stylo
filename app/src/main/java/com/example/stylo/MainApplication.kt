package com.example.stylo

import android.app.Application
import com.example.stylo.data.NotesMetaDataDatabase

class MainApplication : Application() {
    val database: NotesMetaDataDatabase by lazy { NotesMetaDataDatabase.getInstance(this) }
    val notesDao by lazy { database.notesMetaDataDao() }
    val notesDataSource by lazy { database.notesDataSource() }
}