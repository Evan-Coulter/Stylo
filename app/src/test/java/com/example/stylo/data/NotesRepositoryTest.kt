package com.example.stylo.data

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.stylo.data.database.NotesMetaDataDao
import com.example.stylo.data.database.NotesMetaDataDatabase
import com.example.stylo.data.exceptions.*
import com.example.stylo.data.fileaccess.FileAccessSource
import com.example.stylo.data.model.RoomFolder
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
    @VisibleForTesting
    lateinit var notesMetaDataDao: NotesMetaDataDao
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
    fun `test get note by ID`() {
        val noteBuilder = RoomNoteBuilder()
            .setTitle("Hello World")
            .setContent("Goodbye World")
        noteBuilder.setFileName(repository.getCurrentOrGenerateNewFileName(noteBuilder.build()))
        val noteID = repository.add(noteBuilder.build())
        val note = repository.getNote(noteID)
        assertEquals("Hello World", note.title)
        assertEquals("Goodbye World", note.content)
        assertEquals("Hello World1", note.filePath)
    }

    @Test
    fun `test get folder by ID`() {
        val folderBuilder = RoomFolderBuilder()
            .setColor("Blue")
            .setName("Hi")
        val folderID = repository.add(folderBuilder.build())
        val folder = repository.getFolder(folderID)
        assertEquals(1, folder.uid)
        assertEquals("Blue", folder.color)
        assertEquals("Hi", folder.name)
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
        assertEquals(100, retrievedNotes.map{it.filePath}.distinct().size)
    }

    @Test
    fun `test insert one note without a filepath`() {
        val note = RoomNoteBuilder()
            .setTitle("Hello World!")
            .setContent("Hello World!")
            .build()
        assertThrows(FilePathNotSetException::class.java) {
            repository.add(note)
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
        val note = RoomNoteBuilder().build()
        assertThrows(NoteNotInitializedException::class.java) {
            repository.add(note)
        }
    }

    @Test
    fun `test attempt to delete missing note`() {
        val note = RoomNoteBuilder()
            .setTitle("My Favourite Note")
            .setContent("Hello World!")
        note.setFileName(repository.getCurrentOrGenerateNewFileName(note.build()))
        repository.add(note.build())
        note.setUID(1000)
        assertThrows(NoteNotFoundException::class.java) {
            repository.delete(note.build())
        }
        assertEquals(1, repository.getAllNotes().size)
        note.setUID(1)
        repository.delete(note.build())
        assertEquals(0, repository.getAllNotes().size)
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
        val noteBuilder = RoomNoteBuilder()
            .setTitle("Hi")
            .setContent("World")
        noteBuilder.setFileName(repository.getCurrentOrGenerateNewFileName(noteBuilder.build()))
        val folderBuilder = RoomFolderBuilder()
            .setName("Notes")
            .setColor("Blue")
        val noteId = repository.add(noteBuilder.build())
        val folderId = repository.add(folderBuilder.build())
        val note = repository.getAllNotes().first { it.uid == noteId }
        val folder = repository.getAllFolders().first { it.uid == folderId }
        repository.addNoteToFolder(note, folder)
        val belongsTo = notesMetaDataDao.getAllBelongsTo()
        assertEquals(1, belongsTo.size)
    }

    @Test
    fun `test attempt to add missing note to folder`() {
        val noteBuilder = RoomNoteBuilder()
            .setTitle("Hi")
            .setContent("World")
        noteBuilder.setFileName(repository.getCurrentOrGenerateNewFileName(noteBuilder.build()))
        val folderBuilder = RoomFolderBuilder()
            .setName("Notes")
            .setColor("Blue")
        val folderId = repository.add(folderBuilder.build())
        val folder = repository.getAllFolders().first { it.uid == folderId }
        assertThrows(NoteNotFoundException::class.java) {
            repository.addNoteToFolder(noteBuilder.build(), folder)
        }
        assertEquals(0, notesMetaDataDao.getAllBelongsTo().size)
        val noteId = repository.add(noteBuilder.build())
        val note = repository.getAllNotes().first { it.uid == noteId }
        repository.addNoteToFolder(note, folder)
        assertEquals(1, notesMetaDataDao.getAllBelongsTo().size)
    }

    @Test
    fun `test attempt to add note to missing folder`() {
        val noteBuilder = RoomNoteBuilder()
            .setTitle("Hi")
            .setContent("World")
        noteBuilder.setFileName(repository.getCurrentOrGenerateNewFileName(noteBuilder.build()))
        val folderBuilder = RoomFolderBuilder()
            .setName("Notes")
            .setColor("Blue")
        val noteId = repository.add(noteBuilder.build())
        val note = repository.getAllNotes().first { it.uid == noteId }
        assertThrows(FolderNotFoundException::class.java) {
            repository.addNoteToFolder(note, folderBuilder.build())
        }
        assertEquals(0, notesMetaDataDao.getAllBelongsTo().size)
        val folderId = repository.add(folderBuilder.build())
        val folder = repository.getAllFolders().first { it.uid == folderId }
        repository.addNoteToFolder(note, folder)
        assertEquals(1, notesMetaDataDao.getAllBelongsTo().size)
    }

    @Test
    fun `test add one note to multiple folders`() {
        val noteBuilder = RoomNoteBuilder()
            .setTitle("Hello World!")
            .setContent("My Note")
        noteBuilder.setFileName(repository.getCurrentOrGenerateNewFileName(noteBuilder.build()))
        val noteID = repository.add(noteBuilder.build())
        val note = repository.getAllNotes().first { it.uid == noteID }
        val folders = mutableListOf<RoomFolder>()
        val folderBuilder = RoomFolderBuilder()
            .setName("Folder 1")
            .setColor("Blue")
        folders.add(folderBuilder.build())
        folderBuilder.setName("Folder 2")
        folderBuilder.setColor("Green")
        folders.add(folderBuilder.build())
        folderBuilder.setName("Folder 3")
        folderBuilder.setColor("Red")
        folders.add(folderBuilder.build())
        folders.forEach {
            val folderID = repository.add(it)
            val folder = repository.getAllFolders().first { fd -> fd.uid == folderID }
            repository.addNoteToFolder(note, folder)
        }
        val belongsTo = notesMetaDataDao.getAllBelongsTo()
        assertEquals(3, belongsTo.size)
    }

    @Test
    fun `test add many notes to one folder`() {
        val noteBuilder = RoomNoteBuilder()
        val notes = mutableListOf<RoomNote>()

        noteBuilder.setTitle("Hello").setContent("World")
        noteBuilder.setFileName(repository.getCurrentOrGenerateNewFileName(noteBuilder.build()))
        var noteID = repository.add(noteBuilder.build())
        notes.add(repository.getAllNotes().first { it.uid ==  noteID})

        noteBuilder.setTitle("Goodbye").setContent("World")
        noteBuilder.setFileName(repository.getCurrentOrGenerateNewFileName(noteBuilder.build()))
        noteID = repository.add(noteBuilder.build())
        notes.add(repository.getAllNotes().first { it.uid ==  noteID})

        noteBuilder.setTitle("Greetings").setContent("World")
        noteBuilder.setFileName(repository.getCurrentOrGenerateNewFileName(noteBuilder.build()))
        noteID = repository.add(noteBuilder.build())
        notes.add(repository.getAllNotes().first { it.uid ==  noteID})

        val folderBuilder = RoomFolderBuilder()
            .setName("My Notes")
            .setColor("Green")
        val folderID = repository.add(folderBuilder.build())
        val folder = repository.getAllFolders().first { it.uid == folderID }
        notes.forEach {
            repository.addNoteToFolder(it, folder)
        }
        val belongsTo = notesMetaDataDao.getAllBelongsTo()
        val retrievedNotes = repository.getAllNotes()
        assertEquals(3, belongsTo.size)
        assertEquals(3, retrievedNotes.size)
    }

    @Test
    fun `test add many notes to many folder`() {
        val notes = mutableListOf<RoomNote>()
        for (i in 0..4) {
            val noteBuilder = RoomNoteBuilder()
            noteBuilder.setTitle(i.toString())
            noteBuilder.setContent(i.toString())
            noteBuilder.setFileName(repository.getCurrentOrGenerateNewFileName(noteBuilder.build()))
            val noteID = repository.add(noteBuilder.build())
            val note = repository.getAllNotes().first { it.uid == noteID }
            notes.add(note)
        }
        val folderBuilder = RoomFolderBuilder()
        val folders = mutableListOf<RoomFolder>()
        assertEquals(5, repository.getAllNotes().map{it.filePath}.distinct().size)
        notes.forEachIndexed { i: Int, _: RoomNote ->
            folderBuilder.setColor(i.toString())
            folderBuilder.setName(i.toString())
            val folderID = repository.add(folderBuilder.build())
            val folder = repository.getAllFolders().first { it.uid == folderID }
            folders.add(folder)
            notes.forEach { note ->
                repository.addNoteToFolder(note, folder)
            }
        }
        val belongsTo = notesMetaDataDao.getAllBelongsTo()
        assertEquals(25, belongsTo.size)
        for (i in 1..5) {
            belongsTo.filter { it.note == i }.size.apply {
                assertEquals(5, this)
            }
        }
    }

    @Test
    fun `test add one note to one folder then delete folder`() {
        //Given a note added to a single folder
        val noteBuilder = RoomNoteBuilder()
            .setTitle("Hello")
            .setContent("World")
        noteBuilder.setFileName(repository.getCurrentOrGenerateNewFileName(noteBuilder.build()))
        val folderBuilder = RoomFolderBuilder()
            .setColor("Blue")
            .setName("Homework")
        val noteID = repository.add(noteBuilder.build())
        val folderID = repository.add(folderBuilder.build())
        val note = repository.getNote(noteID)
        val folder = repository.getFolder(folderID)
        repository.addNoteToFolder(note, folder)
        assertEquals(1, notesMetaDataDao.getAllBelongsTo().size)
        assertEquals(1, notesMetaDataDao.getAllBelongsTo()[0].note)
        assertEquals(1, notesMetaDataDao.getAllBelongsTo()[0].folder)
        assertEquals(1, repository.getAllFolders().size)
        //When folder is deleted
        repository.delete(folder)
        //Then we don't have any belongsTo relations anymore and folder table is empty
        assertEquals(0, notesMetaDataDao.getAllBelongsTo().size)
        assertEquals(0, repository.getAllFolders().size)
    }

    @Test
    fun `test add one note to many folder then delete one folder`() {
        val noteBuilder = RoomNoteBuilder()
            .setTitle("Hello")
            .setContent("World")
        noteBuilder.setFileName(repository.getCurrentOrGenerateNewFileName(noteBuilder.build()))
        val noteID = repository.add(noteBuilder.build())
        val note = repository.getNote(noteID)
        val folderBuilder = RoomFolderBuilder()
        for (i in 1..5) {
            folderBuilder.setColor(i.toString())
            folderBuilder.setName(i.toString())
            val folderID = repository.add(folderBuilder.build())
            val folder = repository.getFolder(folderID)
            repository.addNoteToFolder(note, folder)
        }
        assertEquals(5, notesMetaDataDao.getAllBelongsTo().size)
        assertEquals(listOf(1, 2, 3, 4, 5), notesMetaDataDao.getAllBelongsTo().map { it.folder })
        repository.delete(repository.getFolder(3))
        assertEquals(4, notesMetaDataDao.getAllBelongsTo().size)
        assertEquals(listOf(1, 2, 4, 5), notesMetaDataDao.getAllBelongsTo().map { it.folder })
    }

    @Test
    fun `test delete one note from folder`() {
        val noteBuilder = RoomNoteBuilder()
            .setTitle("Hello")
            .setContent("World")
        noteBuilder.setFileName(repository.getCurrentOrGenerateNewFileName(noteBuilder.build()))
        val noteID = repository.add(noteBuilder.build())
        val note = repository.getNote(noteID)
        val folderBuilder = RoomFolderBuilder()
            .setName("Notes")
            .setColor("Blue")
        val folderID = repository.add(folderBuilder.build())
        val folder = repository.getFolder(folderID)
        repository.addNoteToFolder(note, folder)
        assertEquals(1, notesMetaDataDao.getAllBelongsTo().size)
        repository.deleteNoteFromFolder(note, folder)
        assertEquals(0, notesMetaDataDao.getAllBelongsTo().size)
    }

    @Test
    fun `test edit and save note`() {
        //Given a note that was previously inserted
        val noteBuilder = RoomNoteBuilder()
            .setTitle("Hello")
            .setContent("World")
        noteBuilder.setFileName(repository.getCurrentOrGenerateNewFileName(noteBuilder.build()))
        val noteID = repository.add(noteBuilder.build())
        val note = repository.getNote(noteID)
        //When note had content and title changed
        val newNoteBuilder = RoomNoteBuilder().clone(note)
        newNoteBuilder.setTitle("Goodbye").setContent("World!!!")
        val newNoteID = repository.add(newNoteBuilder.build())
        val newNote = repository.getNote(newNoteID)
        //Then expect note to have the same file name, date created, and repository to have correct number of saved items.
        assertEquals(noteID, newNoteID)
        assertEquals(1, repository.getAllNotes().size)
        assertEquals("Goodbye", repository.getAllNotes()[0].title)
        assertEquals("World!!!", repository.getAllNotes()[0].content)
        assertEquals(note.filePath, newNote.filePath)
    }

    @Test
    fun `test edit and save folder`() {
        //Given a folder that was previously saved
        val folderBuilder = RoomFolderBuilder()
            .setName("Hello")
            .setColor("Blue")
        val folderID = repository.add(folderBuilder.build())
        val folder = repository.getFolder(folderID)
        //When folder had colour and title changed
        val newFolderBuilder = RoomFolderBuilder().clone(folder)
        newFolderBuilder.setName("Notes").setColor("Green")
        val newFolderID = repository.add(newFolderBuilder.build())
        //Then folder should be the same in DB just with different title and colour.
        assertEquals(folder.uid, newFolderID)
        assertEquals(1, repository.getAllFolders().size)
        assertEquals("Notes", repository.getAllFolders()[0].name)
        assertEquals("Green", repository.getAllFolders()[0].color)
    }

    @Test
    fun `test attempt to add note with filename longer than allowed limit`() {
        //Given a new note with a too long title
        val noteBuilder = RoomNoteBuilder()
            .setContent("World")
        for (i in 1..200) {
            noteBuilder.setTitle(noteBuilder.title + "Wow")
        }
        //When attempting to generate filename
        val runnable = { noteBuilder.setFileName(repository.getCurrentOrGenerateNewFileName(noteBuilder.build())) }
        //Then repository throws an error.
        assertThrows(TitleTooLongException::class.java) {
            runnable.invoke()
        }
        assertEquals(0, repository.getAllNotes().size)
    }

    @Test
    fun `test get default folder`() {
        val folder = repository.getDefaultFolder()
        assertEquals(DEFAULT_FOLDER_NAME, folder.name)
        assertEquals(DEFAULT_FOLDER_COLOR, folder.color)
    }
}