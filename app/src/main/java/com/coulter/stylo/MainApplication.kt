package com.coulter.stylo

import android.app.Application
import com.coulter.stylo.data.NotesRepository
import com.coulter.stylo.data.database.NotesMetaDataDatabase
import com.coulter.stylo.data.fileaccess.FileAccessSource

class MainApplication : Application() {
    val database: NotesMetaDataDatabase by lazy { NotesMetaDataDatabase.getInstance(this) }
    private val notesDao by lazy { database.notesMetaDataDao() }
    val notesRepository by lazy { NotesRepository(notesDao, FileAccessSource(applicationContext)) }
}