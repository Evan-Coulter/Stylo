package com.example.stylo.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.stylo.data.model.BelongsToBuilder
import com.example.stylo.data.model.RoomFolder
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
            .setFileName("/filepath")
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
        val noteToDelete = notesMetaDataDao.getAllFolders().first { it.name == "One" }
        notesMetaDataDao.deleteFolder(noteToDelete.uid)

        //Then expect to only have one entry left
        val notes = notesMetaDataDao.getAllFolders()
        assertEquals(1, notes.size)
        assertEquals("Two", notes.first().name)
    }

    @Test
    fun `test add note to folder`() {
        //Given 1 note and 1 folder
        notesMetaDataDao.insert(RoomNoteBuilder().setTitle("Cool Note").build())
        val note = notesMetaDataDao.getAllNotes().first()
        notesMetaDataDao.insert(RoomFolderBuilder().setName("All Folders").build())
        val folder = notesMetaDataDao.getAllFolders().first()

        //When that note belongs to that folder
        notesMetaDataDao.insert(
            BelongsToBuilder()
                .setNote(note.uid)
                .setFolder(folder.uid)
                .build()
        )

        //Then we can retrieve our belongs to relationship between the note and folder
        val belongsToRelationship = notesMetaDataDao.getAllBelongsTo()
        assertEquals(1, belongsToRelationship.size)
        val recoveredNoteID = belongsToRelationship.first().note
        val recoveredFolderID = belongsToRelationship.first().folder
        val recoveredNote = notesMetaDataDao.getAllNotes().first { it.uid == recoveredNoteID }
        val recoveredFolder = notesMetaDataDao.getAllFolders().first { it.uid == recoveredFolderID }
        assertEquals("Cool Note", recoveredNote.title)
        assertEquals("All Folders", recoveredFolder.name)
    }

    @Test
    fun `test one note belongs to many folders`() {
        //Given 1 note and 5 folders
        val currentTime = Calendar.getInstance().time
        val note = RoomNoteBuilder().setTitle("My Note")
            .setDateCreated(currentTime)
            .setDateLastModified(currentTime)
            .build()
        val folders : MutableList<RoomFolder> = mutableListOf()
        for (i in 1..5) {
            folders.add(RoomFolderBuilder().setName(i.toString()).build())
        }
        notesMetaDataDao.insert(note)
        folders.forEach {
            notesMetaDataDao.insert(it)
        }

        //When that note belongs to each folder
        val recoveredNote = notesMetaDataDao.getAllNotes().also {
            assertEquals(1, it.size)
        }.first()
        val recoveredFolders = notesMetaDataDao.getAllFolders()
        assertEquals(5, recoveredFolders.size)
        recoveredFolders.forEach {
            notesMetaDataDao.insert(
                BelongsToBuilder()
                    .setNote(recoveredNote.uid)
                    .setFolder(it.uid)
                    .build()
            )
        }

        //Then we expect to be able to recover a belongs to relation ship for each folder to this note
        val belongsTo = notesMetaDataDao.getAllBelongsTo()
        assertEquals(5, belongsTo.size)
        belongsTo.forEach { relation ->
            val belongsToNote = notesMetaDataDao.getAllNotes().first { it.uid == relation.note }
            val belongsToFolder = notesMetaDataDao.getAllFolders().first { it.uid == relation.folder }
            assertEquals("My Note", belongsToNote.title)
            assertEquals(true, listOf(1, 2, 3, 4, 5).contains(belongsToFolder.name.toInt()))
        }
    }
}