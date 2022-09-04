package com.example.stylo.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.stylo.data.model.RoomFolderBuilder
import com.example.stylo.data.model.RoomNoteBuilder
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.*

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
    fun `test setting all fields insert then get`() {
        //Given a unique entity
        val currentTime = Calendar.getInstance().time
        val note = RoomNoteBuilder()
            .setTitle("My Awesome Note")
            .setDateCreated(currentTime)
            .setDateLastModified(currentTime)
            .setFilePath("/filepath")
            .build()

        //When inserted and retrieved
        notesMetaDataDao.insert(note)
        val notes = notesMetaDataDao.getAllNotes()

        //Then expect to recover that same entity
        assertEquals(1, notes.size)
        val recoveredNote = notes.first()
        assertEquals("My Awesome Note", recoveredNote.title)
        assertEquals(currentTime, recoveredNote.dateCreated)
        assertEquals(currentTime, recoveredNote.dateLastSaved)
        assertEquals("/filepath", recoveredNote.filePath)
    }

    @Test
    fun `test insert then update and get`() {
        //Given one unique entry
        val currentTime = Calendar.getInstance().time
        val builder = RoomNoteBuilder()
            .setTitle("Entry")
        var note = builder.build()

        //When inserted into database, retrieved, and then updated
        notesMetaDataDao.insert(note)
        builder
            .setTitle("New Title")
            .setUID(notesMetaDataDao.getAllNotes()[0].uid)
        note = builder.build()
        notesMetaDataDao.insert(note)

        //Then we expect just one entry that's been updated
        val notes = notesMetaDataDao.getAllNotes()
        assertEquals(1, notes.size)
        note = notes.first()
        assertEquals("New Title", note.title)
    }



    @Test
    fun `test insert and get multiple entries`() {
        //Given 3 unique entries in DB
        notesMetaDataDao.insert(RoomNoteBuilder().setTitle("1").build())
        notesMetaDataDao.insert(RoomNoteBuilder().setTitle("2").build())
        notesMetaDataDao.insert(RoomNoteBuilder().setTitle("3").build())

        //Then we expect 3 unique entries back
        val notes = notesMetaDataDao.getAllNotes()
        assertEquals(3, notes.size)
        notes.forEachIndexed { index, note ->
            assertEquals(index+1, note.title.toInt())
        }
    }

    @Test
    fun `test insert then delete`() {
        //Given 2 notes inside DB
        notesMetaDataDao.insert(RoomNoteBuilder().setTitle("First Note").build())
        notesMetaDataDao.insert(RoomNoteBuilder().setTitle("Second Note").build())

        //When 2nd note has been deleted
        var notes = notesMetaDataDao.getAllNotes()
        val secondNote = notes.first { it.title == "Second Note" }
        notesMetaDataDao.deleteNote(secondNote.uid)

        //Then we should only expect one entry in the DB
        notes = notesMetaDataDao.getAllNotes()
        assertEquals(1, notes.size)
        assertEquals("First Note", notes.first().title)
    }

    @Test
    fun `test insert 2 folders`() {
        //Given 2 folders inserted into DB
        val folder = RoomFolderBuilder().build()
        notesMetaDataDao.insert(folder)
        notesMetaDataDao.insert(folder)

        //Then expect 2 unique folders back
        val folders = notesMetaDataDao.getAllFolders()
        folders.forEachIndexed { index, roomFolder ->
            assertEquals(index+1, roomFolder.uid)
        }
    }

    @Test
    fun `test delete 1 folder`() {
        //Given 2 inserted folders
        notesMetaDataDao.insert(RoomFolderBuilder().setName("One").build())
        notesMetaDataDao.insert(RoomFolderBuilder().setName("Two").build())

        //When deleting one
        val noteToDelete = notesMetaDataDao.getAllFolders().filter {
            it.name == "One"
        }.first()
        notesMetaDataDao.deleteFolder(noteToDelete.uid)

        //Then expect to only have one entry left
        val notes = notesMetaDataDao.getAllFolders()
        assertEquals(1, notes.size)
        assertEquals("Two", notes.first().name)
    }
}