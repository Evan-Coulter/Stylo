package com.example.stylo.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.stylo.data.database.NotesMetaDataDao
import com.example.stylo.data.database.NotesMetaDataDatabase
import com.example.stylo.data.exceptions.FilePathNotSetException
import com.example.stylo.data.exceptions.FolderNotFoundException
import com.example.stylo.data.exceptions.FolderNotInitializedException
import com.example.stylo.data.fileaccess.FileAccessSource
import com.example.stylo.data.model.RoomFolderBuilder
import com.example.stylo.data.model.RoomNote
import com.example.stylo.data.model.RoomNoteBuilder
import org.junit.After
import org.junit.Assert.*
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
            builder.setFileName(repository.getCurrentOrGenerateNewFileName(builder.build()))
            val note = builder.build()
            repository.add(note)

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
                repository.add(builder.build())
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
            repository.add(RoomNoteBuilder().clone(it).setFileName(
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
    fun `test insert one note without a filepath`() {
        val note = RoomNoteBuilder()
            .setTitle("Hello World!")
            .setContent("Hello World!")
            .build()
        try {
            repository.add(note)
            fail()
        } catch (t: FilePathNotSetException) {
            //pass
        }
    }


    @Test
    fun `test delete one note`() {
        val noteBuilder = RoomNoteBuilder()
            .setTitle("Hello")
            .setContent("World")
        noteBuilder.setFileName(repository.getCurrentOrGenerateNewFileName(noteBuilder.build()))
        repository.add(noteBuilder.build())
        var retrievedNotes = repository.getAllNotes()
        assertEquals(1, retrievedNotes.size)
        assertEquals(1, fileAccessor.getAllFilesNames().size)
        assertEquals(retrievedNotes[0].filePath, fileAccessor.getAllFilesNames()[0])
        assertEquals("Hello", retrievedNotes[0].title)
        assertEquals("World", retrievedNotes[0].content)
        repository.delete(retrievedNotes[0])
        retrievedNotes = repository.getAllNotes()
        assertEquals(0, retrievedNotes.size)
        assertEquals(0, fileAccessor.getAllFilesNames().size)
    }

    @Test
    fun `test delete multiple notes`() {
        val notesBuilders = mutableListOf<RoomNoteBuilder>()
        notesBuilders.add(RoomNoteBuilder().setTitle("Hello").setContent("World 1"))
        notesBuilders.add(RoomNoteBuilder().setTitle("Goodbye").setContent("World 2"))
        notesBuilders.add(RoomNoteBuilder().setTitle("Greetings").setContent("World 3"))
        notesBuilders.add(RoomNoteBuilder().setTitle("Salutations").setContent("World 4"))
        notesBuilders.add(RoomNoteBuilder().setTitle("Hey").setContent("World 5"))
        notesBuilders.forEach {
            it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build()))
            repository.add(it.build())
        }
        assertEquals(5, repository.getAllNotes().size)
        assertEquals(5, fileAccessor.getAllFilesNames().size)
        val note2 = repository.getAllNotes().first { it.title == "Goodbye" && it.content == "World 2" }
        val note4 = repository.getAllNotes().first { it.title == "Salutations" && it.content == "World 4" }
        repository.delete(note2)
        repository.delete(note4)
        assertEquals(3, repository.getAllNotes().size)
        assertEquals(3, fileAccessor.getAllFilesNames().size)
        assertFalse(repository.getAllNotes().map { it.title }.contains("Goodbye"))
        assertFalse(repository.getAllNotes().map { it.title }.contains("Salutations"))
        assertTrue(repository.getAllNotes().map { it.title }.contains("Hello"))
        assertTrue(repository.getAllNotes().map { it.title }.contains("Greetings"))
        assertTrue(repository.getAllNotes().map { it.title }.contains("Hey"))
    }

    @Test
    fun `test attempt to create uninitialized note`() {
        fail()
    }

    @Test
    fun `test attempt to delete missing note`() {
        fail()
    }

    @Test
    fun `test create new folder`() {
        val folder = RoomFolderBuilder()
            .setColor("Blue")
            .setName("Homework")
            .build()
        repository.add(folder)
        val folders = repository.getAllFolders()
        assertEquals(1, folders.size)
        assertEquals("Blue", folders[0].color)
        assertEquals("Homework", folders[0].name)
        assertNotEquals(0, folders[0].uid)
    }

    @Test
    fun `test attempt to create uninitialized folder`() {
        val folder = RoomFolderBuilder().build()
        assertThrows(FolderNotInitializedException::class.java) {
            repository.add(folder)
        }
    }

    @Test
    fun `test delete folder`() {
        val folderBuilder = RoomFolderBuilder()
            .setName("Chores")
            .setColor("Green")
        repository.add(folderBuilder.build())
        assertEquals(1, repository.getAllFolders().size)
        assertEquals("Green", repository.getAllFolders()[0].color)
        assertEquals("Chores", repository.getAllFolders()[0].name)
        repository.delete(repository.getAllFolders()[0])
        assertEquals(0, repository.getAllFolders().size)
    }

    @Test
    fun `test attempt to delete missing folder`() {
        val folderBuilder = RoomFolderBuilder()
            .setName("Chores")
            .setColor("Green")
        repository.add(folderBuilder.build())
        assertEquals(1, repository.getAllFolders().size)
        assertEquals("Green", repository.getAllFolders()[0].color)
        assertEquals("Chores", repository.getAllFolders()[0].name)
        folderBuilder.setUID(100)
        assertThrows(FolderNotFoundException::class.java) {
            repository.delete(folderBuilder.build())
        }
        assertEquals(1, repository.getAllFolders().size)
        folderBuilder.setUID(1)
        repository.delete(folderBuilder.build())
        assertEquals(0, repository.getAllFolders().size)
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

    @Test
    fun `test add note to missing folder`() {
        fail()
    }

    @Test
    fun `test delete note from missing folder`() {
        fail()
    }
}