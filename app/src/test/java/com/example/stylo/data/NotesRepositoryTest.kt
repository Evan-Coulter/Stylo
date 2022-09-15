package com.example.stylo.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.stylo.data.database.NotesMetaDataDao
import com.example.stylo.data.database.NotesMetaDataDatabase
import com.example.stylo.data.fileaccess.FileAccessSource
import com.example.stylo.data.model.RoomNote
import com.example.stylo.data.model.RoomNoteBuilder
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
class NotesRepositoryTest {
    private lateinit var notesMetaDataDao: NotesMetaDataDao
    private lateinit var database: NotesMetaDataDatabase
    private lateinit var repository: NotesRepository
    private lateinit var fileAccessor: FileAccessSource

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, NotesMetaDataDatabase::class.java)
            .allowMainThreadQueries() // for robolectric support
            .build()
        notesMetaDataDao = database.notesMetaDataDao()
        fileAccessor = FileAccessSource(ApplicationProvider.getApplicationContext())
        repository = NotesRepository(notesMetaDataDao, fileAccessor)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `test add and retrieve new note with its content`() {
        //Given a new note with content that we want to add to repository
        val currentTime = Calendar.getInstance().time
        val builder = RoomNoteBuilder()
            .setTitle("My New Note")
            .setContent("My New Note's Content")
            .setDateCreated(currentTime)
            .setDateLastModified(currentTime)

        try {
            //When inserted into repository
            builder.setTitle(repository.getCurrentOrGenerateNewFileName(builder.build()))
            val note = builder.build()
            repository.addNote(note)

            //Then we should be able to recover this note from repository with all fields intact
            val retrievedNote = repository.getAllNotes().also {
                assertEquals(1, it.size)
            }.first()
            assertEquals(note.title, retrievedNote.title)
            assertEquals(note.content, retrievedNote.content)
            assertEquals(note.dateCreated, retrievedNote.dateCreated)
            assertEquals(note.dateLastSaved, retrievedNote.dateLastSaved)
        } catch (t: Throwable) {
            t.printStackTrace()
            fail()
        }
    }

    @Test
    fun `test insert multiple notes`() {
        //Given 3 unique notes
        val builder = RoomNoteBuilder()
        val notes = listOf(
            builder.setContent("1)Hello World!").setTitle("The First Note").build(),
            builder.setContent("2)Goodbye World!").setTitle("The Second Note").build(),
            builder.setContent("3)Greetings!").setTitle("The Third Note").build()
        )

        try {
            //When inserted into repository
            notes.forEach {
                builder.clone(it).setFileName(repository.getCurrentOrGenerateNewFileName(builder.build()))
                repository.addNote(builder.build())
            }

            //Then expect to be able to retrieve all our notes
            val retrievedNotes = repository.getAllNotes()
            assertEquals(notes.size, retrievedNotes.size)
            assertEquals(
                notes.first {it.title == "The First Note"}.content,
                retrievedNotes.first {it.title == "The First Note"}.content
            )
            assertEquals(
                notes.first {it.title == "The Second Note"}.content,
                retrievedNotes.first {it.title == "The Second Note"}.content
            )
            assertEquals(
                notes.first {it.title == "The Third Note"}.content,
                retrievedNotes.first {it.title == "The Third Note"}.content
            )
        } catch (t: Throwable) {
            t.printStackTrace()
            fail()
        }
    }

    @Test
    fun `test insert lots of notes`() {
        //Given 100 unique note entries
        val notes = mutableListOf<RoomNote>()
        for (i in 1..100) {
            notes.add(RoomNoteBuilder().setContent(i.toString()).setTitle(i.toString()).build())
        }

        //When inserted into repository
        notes.forEach {
            repository.addNote(RoomNoteBuilder().clone(it).setFileName(
                repository.getCurrentOrGenerateNewFileName(it)
            ).build())
        }

        //Then expect to be able to recover all our notes
        val retrievedNotes = repository.getAllNotes()
        assertEquals(notes.size, retrievedNotes.size)
        for (i in 1..100) {
            val note = notes.first { it.title == i.toString() }
            val retrievedNote = retrievedNotes.first { it.title == i.toString() }
            assertEquals(note.content, retrievedNote.content)
        }
    }


    @Test
    fun `test delete one note`() {
        fail()
    }

    @Test
    fun `test delete multiple notes`() {
        fail()
    }

    @Test
    fun `test add one note to one folder`() {
        fail()
    }

    @Test
    fun `test add one note to multiple folders`() {
        fail()
    }

    @Test
    fun `test add many notes to one folder`() {
        fail()
    }

    @Test
    fun `test add many notes to many folder`() {
        fail()
    }

    @Test
    fun `test add one note to one folder then delete folder`() {
        fail()
    }

    @Test
    fun `test add one note to many folder then delete one folder`() {
        fail()
    }
}