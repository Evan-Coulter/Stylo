package com.example.stylo.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.stylo.data.database.NotesMetaDataDao
import com.example.stylo.data.database.NotesMetaDataDatabase
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class NotesRepositoryTest {
    private lateinit var notesMetaDataDao: NotesMetaDataDao
    private lateinit var database: NotesMetaDataDatabase
    private lateinit var repository: NotesRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, NotesMetaDataDatabase::class.java)
            .allowMainThreadQueries() // for robolectric support
            .build()
        notesMetaDataDao = database.notesMetaDataDao()
        repository = NotesRepository(notesMetaDataDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testAddNoteToRepository() {
        //RuntimeEnvironment.getApplication().getFil
    }
}