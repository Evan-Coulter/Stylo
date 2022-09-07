package com.example.stylo.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.stylo.data.database.NotesMetaDataDao
import com.example.stylo.data.database.NotesMetaDataDatabase
import com.example.stylo.data.fileaccess.FileAccessSource
import com.example.stylo.data.model.RoomNoteBuilder
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.*

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
        repository = NotesRepository(notesMetaDataDao, FileAccessSource(ApplicationProvider.getApplicationContext()))
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `test add and retrieve new note with its content`() {
        //Given a new note with content that we want to add to repository
        val currentTime = Calendar.getInstance().time
        val note = RoomNoteBuilder()
            .setTitle("My New Note")
            .setContent("My New Note's Content")
            .setDateCreated(currentTime)
            .setDateLastModified(currentTime)
            .build()

        //When inserted into repository
        repository.addNote(note)

        //Then we should be able to recover this note from repository with all fields intact
        val retrievedNote = repository.getAllNotes().also {
            assertEquals(1, it.size)
        }.first()
        assertEquals(note.title, retrievedNote.title)
        assertEquals(note.content, retrievedNote.content)
        assertEquals(note.dateCreated, retrievedNote.dateCreated)
        assertEquals(note.dateLastSaved, retrievedNote.dateLastSaved)
    }
}