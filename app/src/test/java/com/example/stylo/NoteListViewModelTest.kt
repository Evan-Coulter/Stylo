package com.example.stylo

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.stylo.data.DEFAULT_FOLDER_COLOR
import com.example.stylo.data.DEFAULT_FOLDER_NAME
import com.example.stylo.data.NotesRepository
import com.example.stylo.data.database.NotesMetaDataDao
import com.example.stylo.data.database.NotesMetaDataDatabase
import com.example.stylo.data.fileaccess.FileAccessSource
import com.example.stylo.data.model.RoomFolderBuilder
import com.example.stylo.data.model.RoomNoteBuilder
import com.example.stylo.list.NoteListEvent
import com.example.stylo.list.NoteListViewModel
import com.example.stylo.list.NoteListViewState
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NoteListViewModelTest {
    @VisibleForTesting
    lateinit var notesMetaDataDao: NotesMetaDataDao
    private lateinit var database: NotesMetaDataDatabase
    private lateinit var repository: NotesRepository
    private lateinit var fileAccessor: FileAccessSource

    lateinit var viewModel: NoteListViewModel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, NotesMetaDataDatabase::class.java)
            .allowMainThreadQueries() // for robolectric support
            .build()
        notesMetaDataDao = database.notesMetaDataDao()
        fileAccessor = FileAccessSource(ApplicationProvider.getApplicationContext())
        repository = NotesRepository(notesMetaDataDao, fileAccessor)
        viewModel = NoteListViewModel(repository)
    }

    @After
    fun tearDown() {
        database.close()
    }


    @Test
    fun `test load initial empty list state`() {
        assertTrue(viewModel.uiState.value is NoteListViewState.LoadingState)
        viewModel._eventListener.value = NoteListEvent.PageLoaded
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowBasicListState)
    }

    @Test
    fun `test load initial full list state`() {
        //Given a single note has been added to our repository already
        val noteBuilder = RoomNoteBuilder()
            .setContent("Hello")
            .setTitle("World")
        noteBuilder.setFileName(repository.getCurrentOrGenerateNewFileName(noteBuilder.build()))
        repository.add(noteBuilder.build())
        //When the note list fragment has loaded.
        viewModel._eventListener.value = NoteListEvent.PageLoaded
        //Then we should expect to retrieve that note when the page.
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowBasicListState)
        val state = viewModel.uiState.value as NoteListViewState.ShowBasicListState
        assertEquals(1, state.list.size)
        assertEquals("World", state.list[0].title)
        assertEquals("Hello", state.list[0].content)
        assertEquals(1, state.list[0].uid)
    }

    @Test
    fun `test load initial full list state with multiple notes and folders`() {
        //Given 5 new notes added to the All Notes folder
        val folderBuilder = RoomFolderBuilder().clone(repository.getDefaultFolder())
        var folderID = repository.add(folderBuilder.build())
        var folder = repository.getFolder(folderID)
        for (i in 0..4) {
            val noteBuilder = RoomNoteBuilder()
            noteBuilder.setContent(i.toString())
            noteBuilder.setTitle(i.toString())
            noteBuilder.setFileName(repository.getCurrentOrGenerateNewFileName(noteBuilder.build()))
            val noteID = repository.add(noteBuilder.build())
            val note = repository.getNote(noteID)
            repository.addNoteToFolder(note, folder)
        }
        //And given 1 of those notes added to the Homework folder
        val noteID2 = repository.getNote(2)
        folderBuilder.setName("Homework").setColor("Green")
        folderID = repository.add(folderBuilder.build())
        folder = repository.getFolder(folderID)
        repository.addNoteToFolder(noteID2, folder)
        //When note list fragment is loaded
        viewModel._eventListener.value = NoteListEvent.PageLoaded
        //Then expect we're in the showing basic list state and can retrieve all our notes and their folder relations
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowBasicListState)
        val state = viewModel.uiState.value as NoteListViewState.ShowBasicListState
        assertEquals(5, state.list.size)
        for (i in 0..4) {
            val note = state.list[i]
            assertEquals(i+1, note.uid)
            assertEquals(i.toString(), note.title)
            assertEquals(i.toString(), note.content)
        }
        assertEquals(DEFAULT_FOLDER_NAME, state.folder.name)
        assertEquals(DEFAULT_FOLDER_COLOR, state.folder.color)
        val belongsTo = notesMetaDataDao.getAllBelongsTo()
        assertEquals(6, belongsTo.size)
        assertEquals(5, belongsTo.filter{it.folder == 1}.size)
        assertEquals(1, belongsTo.filter{it.folder == 2}.size)
        val homeworkFolderBelongsToRelation = belongsTo.first { it.folder == 2 }
        assertEquals(2, homeworkFolderBelongsToRelation.note)
        assertEquals(2, homeworkFolderBelongsToRelation.folder)
    }

    @Test
    fun `test switching folder changes visible notes`() {
        fail()
    }

    @Test
    fun `test when app is closed and reopened, previously selected folder is still open`() {
        //TODO implement lifecycle handling of private state values in view model
        fail()
    }

    @Test
    fun `test card list switch view button pushed`() {
        fail()
    }

    @Test
    fun `test when app is closed and reopened, previously selected card or list view switch is still open`() {
        //TODO implement lifecycle handling of private state values in view model
        fail()
    }

    @Test
    fun `test help button pushed should show help dialog`() {
        fail()
    }

    @Test
    fun `test logo button pushed should show cool effect`() {
        fail()
    }

    @Test
    fun `test folder button with no folders saved pushed should open side tab with all notes folder`() {
        fail()
    }

    @Test
    fun `test add folder button clicked should show create new folder dialog`() {
        fail()
    }

    @Test
    fun `test search button pushed`() {
        fail()
    }


    @Test
    fun `test note pushed`() {
        fail()
    }

    @Test
    fun `test fab pushed`() {
        fail()
    }
}