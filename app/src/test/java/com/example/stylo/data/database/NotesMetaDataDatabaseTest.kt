package com.example.stylo.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.stylo.data.model.RoomNoteBuilder
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NotesMetaDataDatabaseTest {
    private lateinit var notesMetaDataDao: NotesMetaDataDao
    private lateinit var database: NotesMetaDataDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, NotesMetaDataDatabase::class.java)
            .allowMainThreadQueries() // for robolectric support
            .build()
        notesMetaDataDao = database.notesMetaDataDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `test insert and get 3 entries`() {
        //Given 3 unique entries in DB
        notesMetaDataDao.insert(RoomNoteBuilder().setTitle("1").create())
        notesMetaDataDao.insert(RoomNoteBuilder().setTitle("2").create())
        notesMetaDataDao.insert(RoomNoteBuilder().setTitle("3").create())

        //Then we expect 3 unique entries back
        val notes = notesMetaDataDao.getAll()
        assertEquals(3, notes.size)
        notes.forEachIndexed { index, note ->
            assertEquals(index+1, note.title.toInt())
        }
    }

    @Test
    fun `test insert then delete works as expected`() {
        //Given 2 notes inside DB
        notesMetaDataDao.insert(RoomNoteBuilder().setTitle("First Note").create())
        notesMetaDataDao.insert(RoomNoteBuilder().setTitle("Second Note").create())

        //When 2nd note has been deleted
        var notes = notesMetaDataDao.getAll()
        val secondNote = notes.first { it.title == "Second Note" }
        notesMetaDataDao.delete(secondNote.uid)

        //Then we should only expect one entry in the DB
        notes = notesMetaDataDao.getAll()
        assertEquals(1, notes.size)
        assertEquals("First Note", notes.first().title)
    }
}