package com.example.stylo

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.content.Context
import android.content.SharedPreferences
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
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class NoteListViewModelTest {
    @VisibleForTesting
    lateinit var notesMetaDataDao: NotesMetaDataDao
    private lateinit var database: NotesMetaDataDatabase
    private lateinit var repository: NotesRepository
    private lateinit var fileAccessor: FileAccessSource
    private lateinit var sharedPreferences: SharedPreferences

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
        sharedPreferences = RuntimeEnvironment.getApplication().getSharedPreferences("test", Context.MODE_PRIVATE)
        viewModel = NoteListViewModel(repository, sharedPreferences)
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
        assertEquals(1, state.notes.size)
        assertEquals("World", state.notes[0].title)
        assertEquals("Hello", state.notes[0].content)
        assertEquals(1, state.notes[0].uid)
    }

    @Test
    fun `test load initial full list state with multiple notes and folders`() {
        //Given 5 new notes added to the All Notes folder
        val folderBuilder = RoomFolderBuilder().clone(repository.getDefaultFolder()).setUID(1)
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
        folderBuilder.setName("Homework").setColor("Green").setUID(0)
        folderID = repository.add(folderBuilder.build())
        folder = repository.getFolder(folderID)
        repository.addNoteToFolder(noteID2, folder)

        //When note list fragment is loaded
        viewModel._eventListener.value = NoteListEvent.PageLoaded

        //Then expect we're in the showing basic list state and can retrieve all our notes and their folder relations
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowBasicListState)
        val state = viewModel.uiState.value as NoteListViewState.ShowBasicListState
        assertEquals(5, state.notes.size)
        for (i in 0..4) {
            val note = state.notes[i]
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
        //Given 2 notes in a chores folder.
        var folderBuilder = RoomFolderBuilder()
            .setName("Chores")
            .setColor("Green")
        var folderID = repository.add(folderBuilder.build())
        var folder = repository.getFolder(folderID)
        var noteBuilder = RoomNoteBuilder()
            .setTitle("Chores 1")
            .setContent("Chores 1 Content")
        noteBuilder.setFileName(repository.getCurrentOrGenerateNewFileName(noteBuilder.build()))
        var note = repository.getNote(repository.add(noteBuilder.build()))
        repository.addNoteToFolder(note, folder)
        noteBuilder = RoomNoteBuilder()
            .setTitle("Chores 2")
            .setContent("Chores 2 Content")
        noteBuilder.setFileName(repository.getCurrentOrGenerateNewFileName(noteBuilder.build()))
        note = repository.getNote(repository.add(noteBuilder.build()))
        repository.addNoteToFolder(note, folder)

        //And given 2 notes in a homework folder.
        folderBuilder = RoomFolderBuilder()
            .setName("Homework")
            .setColor("Blue")
        folderID = repository.add(folderBuilder.build())
        folder = repository.getFolder(folderID)
        noteBuilder = RoomNoteBuilder()
            .setTitle("Homework 1")
            .setContent("Homework 1 Content")
        noteBuilder.setFileName(repository.getCurrentOrGenerateNewFileName(noteBuilder.build()))
        note = repository.getNote(repository.add(noteBuilder.build()))
        repository.addNoteToFolder(note, folder)
        noteBuilder = RoomNoteBuilder()
            .setTitle("Homework 2")
            .setContent("Homework 2 Content")
        noteBuilder.setFileName(repository.getCurrentOrGenerateNewFileName(noteBuilder.build()))
        note = repository.getNote(repository.add(noteBuilder.build()))
        repository.addNoteToFolder(note, folder)

        //When we first open the note list fragment
        viewModel._eventListener.value = NoteListEvent.PageLoaded

        //Then expect to be in basic list state with All Folders selected and we can see all 4 notes
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowBasicListState)
        var state = viewModel.uiState.value as NoteListViewState.ShowBasicListState
        assertEquals(4, state.notes.size)
        assertEquals(DEFAULT_FOLDER_NAME, state.folder.name)
        assertEquals(DEFAULT_FOLDER_COLOR, state.folder.color)

        //When we change to the chores folder
        viewModel._eventListener.value = NoteListEvent.ChangeFolderButtonPushed(2)

        //Then expect to be in basic list state with Chores selected and we can see 2 notes
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowBasicListState)
        state = viewModel.uiState.value as NoteListViewState.ShowBasicListState
        assertEquals(2, state.notes.size)
        assertEquals("Chores", state.folder.name)
        assertEquals("Green", state.folder.color)
        assertTrue(state.notes.map{it.title}.contains("Chores 1"))
        assertTrue(state.notes.map{it.title}.contains("Chores 2"))

        //When we change to the chores folder
        viewModel._eventListener.value = NoteListEvent.ChangeFolderButtonPushed(3)

        //Then expect to be in basic list state with Homework selected and we can see 2 notes
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowBasicListState)
        state = viewModel.uiState.value as NoteListViewState.ShowBasicListState
        assertEquals(2, state.notes.size)
        assertEquals("Homework", state.folder.name)
        assertEquals("Blue", state.folder.color)
        assertTrue(state.notes.map{it.title}.contains("Homework 1"))
        assertTrue(state.notes.map{it.title}.contains("Homework 2"))
    }

    @Test
    fun `test when app is closed and reopened, previously selected folder is still open`() {
        //Given 3 folders in our repository
        repository.add(RoomFolderBuilder()
            .setName("Homework")
            .setColor("Green")
            .build())
        val folderID = repository.add(RoomFolderBuilder()
            .setName("Chores")
            .setColor("Blue")
            .build())

        //When we switch the selected folder and the "restart" the app
        viewModel._eventListener.value = NoteListEvent.ChangeFolderButtonPushed(folderID)
        val newViewModel = NoteListViewModel(repository, sharedPreferences)
        newViewModel._eventListener.value = NoteListEvent.PageLoaded

        //Then expect when we open to the app that the new folder is still selected
        assertTrue(newViewModel.uiState.value is NoteListViewState.ShowBasicListState)
        val state = newViewModel.uiState.value as NoteListViewState.ShowBasicListState
        assertEquals("Chores", state.folder.name)
        assertEquals("Blue", state.folder.color)
    }

    @Test
    fun `test when app starts we have the All Notes folder already visible`() {
        //Given app just started
        //When note list fragment is first loaded for the first time.
        viewModel._eventListener.value = NoteListEvent.PageLoaded
        //Then we should see All Notes folder with no notes saved and isListView = True.
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowBasicListState)
        val state = viewModel.uiState.value as NoteListViewState.ShowBasicListState
        assertEquals(repository.getDefaultFolder().name, state.folder.name)
        assertEquals(repository.getDefaultFolder().color, state.folder.color)
        assertEquals(0, state.notes.size)
        assertEquals(true, state.isListView)
    }

    @Test
    fun `test when app is closed and reopened, previously selected card or list view switch is still open`() {
        //Given two notes in the homework folder.
        val homeworkFolder = repository.getFolder(repository.add(RoomFolderBuilder()
            .setName("Homework").setColor("Green").build()))
        for (i in 0..1) {
            val noteBuilder = RoomNoteBuilder()
                .setTitle("$i homework title")
                .setContent("$i homework content")
                .also{it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build()))}
            val note = repository.getNote(repository.add(noteBuilder.build()))
            repository.addNoteToFolder(note, homeworkFolder)
        }
        //And Given two notes in the chores folder
        val choresFolder = repository.getFolder(repository.add(RoomFolderBuilder()
            .setName("Chores").setColor("Blue").build()))
        for (i in 2..3) {
            val noteBuilder = RoomNoteBuilder()
                .setTitle("$i chores title")
                .setContent("$i chores content")
                .also{it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build()))}
            val note = repository.getNote(repository.add(noteBuilder.build()))
            repository.addNoteToFolder(note, choresFolder)
        }
        //When we start the app, switch to the chores folder, and the "restart" the app.
        viewModel._eventListener.value = NoteListEvent.ChangeFolderButtonPushed(choresFolder.uid)
        viewModel._eventListener.value = NoteListEvent.CardListViewSwitchPushed
        val newViewModelInstance = NoteListViewModel(repository, sharedPreferences)
        newViewModelInstance._eventListener.value = NoteListEvent.PageLoaded
        //Then expect that the chores folder is still open and we're still in card view.
        assertTrue(newViewModelInstance.uiState.value is NoteListViewState.ShowBasicListState)
        val state = newViewModelInstance.uiState.value as NoteListViewState.ShowBasicListState
        assertEquals("Chores", state.folder.name)
        assertEquals("Blue", state.folder.color)
        assertEquals(2, state.notes.size)
        assertEquals(false, state.isListView)
    }

    @Test
    fun `test help button pushed should show help dialog`() {
        //Given view model is in show basic list state
        viewModel._eventListener.value = NoteListEvent.PageLoaded
        //When help button is pushed
        viewModel._eventListener.value = NoteListEvent.HelpPushed
        //Then assert we're in show help dialog state
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowHelpDialog)
    }

    @Test
    fun `test folder button with no folders saved pushed should open side tab with all notes folder`() {
        //Given vew model is in show basic list state and we have 4 folders saved already
        viewModel._eventListener.value = NoteListEvent.PageLoaded
        //All Notes folder is already saved.
        repository.add(RoomFolderBuilder().setName("2").setColor("2").build())
        repository.add(RoomFolderBuilder().setName("3").setColor("3").build())
        repository.add(RoomFolderBuilder().setName("4").setColor("4").build())

        //When folder side tray button is pushed
        viewModel._eventListener.value = NoteListEvent.FolderButtonPushed

        //Then assert we are in the Show Folder Tray state with 4 possible options.
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowFoldersTray)
        val state = viewModel.uiState.value as NoteListViewState.ShowFoldersTray
        assertEquals(4, state.folders.size)
        assertTrue(state.folders.map{it.name}.contains(repository.getDefaultFolder().name))
        assertTrue(state.folders.map{it.name}.contains("2"))
        assertTrue(state.folders.map{it.name}.contains("3"))
        assertTrue(state.folders.map{it.name}.contains("4"))

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
    fun `test add note button pushed`() {
        fail()
    }
}