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
import com.example.stylo.data.exceptions.FOLDER_ALREADY_EXISTS_MESSAGE
import com.example.stylo.data.exceptions.FOLDER_TITLE_ERROR_MESSAGE
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
        viewModel._eventListener.value = NoteListEvent.ChangeFolderButtonClicked(2)

        //Then expect to be in basic list state with Chores selected and we can see 2 notes
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowBasicListState)
        state = viewModel.uiState.value as NoteListViewState.ShowBasicListState
        assertEquals(2, state.notes.size)
        assertEquals("Chores", state.folder.name)
        assertEquals("Green", state.folder.color)
        assertTrue(state.notes.map{it.title}.contains("Chores 1"))
        assertTrue(state.notes.map{it.title}.contains("Chores 2"))

        //When we change to the chores folder
        viewModel._eventListener.value = NoteListEvent.ChangeFolderButtonClicked(3)

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
        viewModel._eventListener.value = NoteListEvent.ChangeFolderButtonClicked(folderID)
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
        viewModel._eventListener.value = NoteListEvent.ChangeFolderButtonClicked(choresFolder.uid)
        viewModel._eventListener.value = NoteListEvent.CardListViewSwitchClicked
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
        viewModel._eventListener.value = NoteListEvent.HelpButtonClicked
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
        viewModel._eventListener.value = NoteListEvent.FolderTrayButtonClicked

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
        //Given folder tray is open
        viewModel._eventListener.value = NoteListEvent.FolderTrayButtonClicked
        //When we push the add new folder button
        viewModel._eventListener.value = NoteListEvent.AddNewFolderButtonClicked
        //Then assert we're showing the add new folder dialog
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowCreateFolderDialog)
    }

    @Test
    fun `test success case when adding new folder`() {
        //And Given we're trying in the create new folder dialog
        viewModel._eventListener.value = NoteListEvent.AddNewFolderButtonClicked

        //When we try to add a new folder
        val newFolder = RoomFolderBuilder().setName("Homework").setColor("Green").build()
        viewModel._eventListener.value = NoteListEvent.AttemptToAddNewFolder(newFolder)

        //Then assert we get a success response from repository and can see our new folder is saved.
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowCreateFolderSuccessMessage)
        viewModel._eventListener.value = NoteListEvent.FolderTrayButtonClicked
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowFoldersTray)
        val state = viewModel.uiState.value as NoteListViewState.ShowFoldersTray
        assertEquals(2, state.folders.size)
        assertTrue(state.folders.map{it.name}.contains("Homework"))
        assertTrue(state.folders.map{it.color}.contains("Green"))
        assertTrue(state.folders.map{it.name}.contains(repository.getDefaultFolder().name))
        assertTrue(state.folders.map{it.color}.contains(repository.getDefaultFolder().color))
    }

    @Test
    fun `test fail save duplicate case when adding new folder`() {
        //Given we already have a homework folder saved.
        viewModel._eventListener.value = NoteListEvent.PageLoaded
        viewModel._eventListener.value = NoteListEvent.FolderTrayButtonClicked
        viewModel._eventListener.value = NoteListEvent.AddNewFolderButtonClicked
        val folder = RoomFolderBuilder().setName("Homework").setColor("Green").build()
        viewModel._eventListener.value = NoteListEvent.AttemptToAddNewFolder(folder)

        //When we try to add a new folder with the same name.
        viewModel._eventListener.value = NoteListEvent.PageLoaded
        viewModel._eventListener.value = NoteListEvent.FolderTrayButtonClicked
        viewModel._eventListener.value = NoteListEvent.AddNewFolderButtonClicked
        val newFolder = RoomFolderBuilder().setName("Homework").setColor("Green").build()
        viewModel._eventListener.value = NoteListEvent.AttemptToAddNewFolder(newFolder)

        //Then assert we received an error message
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowCreateFolderErrorMessage)
        var state : NoteListViewState = viewModel.uiState.value as NoteListViewState.ShowCreateFolderErrorMessage
        assertEquals(FOLDER_ALREADY_EXISTS_MESSAGE, (state as NoteListViewState.ShowCreateFolderErrorMessage).errorMessage)
        viewModel._eventListener.value = NoteListEvent.FolderTrayButtonClicked
        state = viewModel.uiState.value as NoteListViewState.ShowFoldersTray
        assertEquals(2, state.folders.size)
    }

    @Test
    fun `test fail save uninitialized case when adding new folder`() {
        //And Given we're trying in the create new folder dialog
        viewModel._eventListener.value = NoteListEvent.AddNewFolderButtonClicked

        //When the new folder is not fully initialized (no title)
        val newFolder = RoomFolderBuilder().setColor("Green").build()
        viewModel._eventListener.value = NoteListEvent.AttemptToAddNewFolder(newFolder)

        //Then assert we receive error messages.
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowCreateFolderErrorMessage)
        var state : NoteListViewState = viewModel.uiState.value as NoteListViewState.ShowCreateFolderErrorMessage
        assertEquals(FOLDER_TITLE_ERROR_MESSAGE, (state as NoteListViewState.ShowCreateFolderErrorMessage).errorMessage)
        viewModel._eventListener.value = NoteListEvent.FolderTrayButtonClicked
        state = viewModel.uiState.value as NoteListViewState.ShowFoldersTray
        assertEquals(1, state.folders.size)
    }


    @Test
    fun `test note pushed`() {
        //Given we have 3 notes saved and we're in basic list state
        for (i in 0..2) {
            val note = RoomNoteBuilder()
                .setTitle("$i title")
                .setContent("$i content")
                .also {it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build()))}
                .build()
            repository.add(note)
        }
        //When a note is pushed
        viewModel._eventListener.value = NoteListEvent.PageLoaded
        viewModel._eventListener.value = NoteListEvent.NoteClicked(noteID = 2)
        //Then assert we're about to open the note editor for that note.
        assertTrue(viewModel.uiState.value is NoteListViewState.OpenNoteEditor)
        val state = viewModel.uiState.value as NoteListViewState.OpenNoteEditor
        assertEquals("1 title", state.note.title)
        assertEquals("1 content", state.note.content)

    }

    @Test
    fun `test open folder editor dialog`() {
        //Given we're in the folder editor dialog and we have 1 folder saved (in addition to all folders)
        viewModel._eventListener.value = NoteListEvent.FolderTrayButtonClicked
        viewModel._eventListener.value = NoteListEvent.AttemptToAddNewFolder(
            RoomFolderBuilder().setName("Homework").setColor("Red").build()
        )
        //When we click on the homework folder's edit button
        viewModel._eventListener.value = NoteListEvent.EditFolderButtonClicked(2)
        //Then we should be seeing the edit folder button dialog with homework folder details.
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowEditFolderDialog)
        val state = viewModel.uiState.value as NoteListViewState.ShowEditFolderDialog
        assertEquals("Homework", state.folder.name)
        assertEquals("Red", state.folder.color)
    }

    @Test
    fun `test delete folder button is pushed from edit folder dialog`() {
        //Given we have two folders saved and we're in the side folder tray.
        viewModel._eventListener.value = NoteListEvent.FolderTrayButtonClicked
        viewModel._eventListener.value = NoteListEvent.AddNewFolderButtonClicked
        viewModel._eventListener.value = NoteListEvent.AttemptToAddNewFolder(
            RoomFolderBuilder().setName("Homework").setColor("Green").build()
        )
        viewModel._eventListener.value = NoteListEvent.AttemptToAddNewFolder(
            RoomFolderBuilder().setName("Chores").setColor("Blue").build()
        )
        //When we click on the chores folder edit button and click delete.
        viewModel._eventListener.value = NoteListEvent.EditFolderButtonClicked(3)
        viewModel._eventListener.value = NoteListEvent.DeleteFolderButtonClicked(3)
        //Then assert that folder doesn't exist anymore
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowFoldersTray)
        val state = viewModel.uiState.value as NoteListViewState.ShowFoldersTray
        assertEquals(2, state.folders.size)
        assertFalse(state.folders.map { it.name }.contains("Chores"))
    }

    @Test
    fun `test edit folder title from edit folder dialog`() {
        //Given we're in the edit folder details dialog
        viewModel._eventListener.value = NoteListEvent.AttemptToAddNewFolder(RoomFolderBuilder()
            .setName("Homework").setColor("Green").build())
        viewModel._eventListener.value = NoteListEvent.EditFolderButtonClicked(folderID = 2)
        //When we try to edit the details of the homework folder
        viewModel._eventListener.value = NoteListEvent.AttemptToEditFolder(
            RoomFolderBuilder().clone(repository.getFolder(2)).setName("Chores").setColor("Blue").build()
        )
        viewModel._eventListener.value = NoteListEvent.FolderTrayButtonClicked
        //Then assert we're back in the folder tray and the folder's details have been updates
        val state = viewModel.uiState.value as NoteListViewState.ShowFoldersTray
        assertEquals(2, state.folders.size)
        val folder = state.folders[1]
        assertEquals("Chores", folder.name)
        assertEquals("Blue", folder.color)
    }

    @Test
    fun `test edit folder title from edit folder dialog failure case`() {
        //Given we're in the edit folder details dialog
        viewModel._eventListener.value = NoteListEvent.AttemptToAddNewFolder(
            RoomFolderBuilder().setName("Homework").setColor("Green").build()
        )
        viewModel._eventListener.value = NoteListEvent.EditFolderButtonClicked(2)

        //When we attempt to save an invalid name
        var folder = (viewModel.uiState.value as NoteListViewState.ShowEditFolderDialog).folder
        var newFolderBuilder = RoomFolderBuilder().clone(folder)
        newFolderBuilder.setName("")
        viewModel._eventListener.value = NoteListEvent.AttemptToEditFolder(newFolderBuilder.build())

        //Then assert we're seeing a folder saving error
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowEditFolderErrorMessage)
        var state = viewModel.uiState.value as NoteListViewState.ShowEditFolderErrorMessage
        assertEquals(FOLDER_TITLE_ERROR_MESSAGE, state.errorMessage)

        //And when we're back in the edit folder dialog and we try another invalid name.
        viewModel._eventListener.value = NoteListEvent.EditFolderButtonClicked(2)
        folder = (viewModel.uiState.value as NoteListViewState.ShowEditFolderDialog).folder
        newFolderBuilder = RoomFolderBuilder().clone(folder)
        newFolderBuilder.setName(repository.getDefaultFolder().name)
        viewModel._eventListener.value = NoteListEvent.AttemptToEditFolder(newFolderBuilder.build())

        //Then assert we're seeing a folder saving error and that we still only have 2 folders saved
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowEditFolderErrorMessage)
        state = viewModel.uiState.value as NoteListViewState.ShowEditFolderErrorMessage
        assertEquals(FOLDER_ALREADY_EXISTS_MESSAGE, state.errorMessage)
        viewModel._eventListener.value = NoteListEvent.FolderTrayButtonClicked
        val newState = viewModel.uiState.value as NoteListViewState.ShowFoldersTray
        assertEquals(2, newState.folders.size)
        assertEquals(repository.getDefaultFolder().name, newState.folders[0].name)
        assertEquals(repository.getDefaultFolder().color, newState.folders[0].color)
        assertEquals("Homework", newState.folders[1].name)
        assertEquals("Green", newState.folders[1].color)
    }

    @Test
    fun `test edit folder color from edit folder dialog`() {
        //Given we're in the edit folder details dialog
        viewModel._eventListener.value = NoteListEvent.AttemptToAddNewFolder(
            RoomFolderBuilder().setName("Homework").setColor("Green").build()
        )
        viewModel._eventListener.value = NoteListEvent.EditFolderButtonClicked(2)

        //When we try to edit a folder's color
        val state = viewModel.uiState.value as NoteListViewState.ShowEditFolderDialog
        val folderBuilder = RoomFolderBuilder().clone(state.folder)
        folderBuilder.setColor("Blue")
        viewModel._eventListener.value = NoteListEvent.AttemptToEditFolder(folderBuilder.build())

        //Then assert the folder has the new color
        var newState = viewModel.uiState.value
        assertTrue(newState is NoteListViewState.ShowEditFolderSuccessMessage)
        viewModel._eventListener.value = NoteListEvent.FolderTrayButtonClicked
        newState = viewModel.uiState.value as NoteListViewState.ShowFoldersTray
        assertEquals(2, newState.folders.size)
        assertEquals("Blue", newState.folders.find { it.name == "Homework" }?.color)
    }

    @Test
    fun `test edit note button is pushed`() {
        //Given we're in basic list state with 2 folders saved
        viewModel._eventListener.value = NoteListEvent.PageLoaded
        repository.add(RoomNoteBuilder()
            .setTitle("Note 1")
            .setContent("Note 1 Content")
            .also{it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build()))}
            .build())
        repository.add(RoomNoteBuilder()
            .setTitle("Note 2")
            .setContent("Note 2 Content")
            .also{it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build()))}
            .build())
        //Given we click the edit folder button on the second folder
        viewModel._eventListener.value = NoteListEvent.EditNoteButtonClicked(2)
        //Then assert we're viewing the edit folder dialog
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowEditNoteDetailsOptions)
        val state = viewModel.uiState.value as NoteListViewState.ShowEditNoteDetailsOptions
        assertEquals("Note 2", state.note.title)
        assertEquals("Note 2 Content", state.note.content)
    }

    @Test
    fun `test delete note from note details editor dialog`() {
        //Given we have one note saved and we're in the basic list state
        val noteID = repository.add(
            RoomNoteBuilder().setTitle("homework 1").setContent("homework 1 content").also {
                it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build()))
            }.build()
        )
        viewModel._eventListener.value = NoteListEvent.PageLoaded

        //When we click the edit note details button
        viewModel._eventListener.value = NoteListEvent.EditNoteButtonClicked(noteID)

        //Then assert we can see the note details dialog
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowEditNoteDetailsOptions)

        //And when we click delete and go back to the basic list state
        viewModel._eventListener.value = NoteListEvent.DeleteNoteButtonClicked(noteID)
        viewModel._eventListener.value = NoteListEvent.PageLoaded

        //Then assert the note is actually deleted
        assertEquals(0, (viewModel.uiState.value as NoteListViewState.ShowBasicListState).notes.size)
    }

    @Test
    fun `test add note to folder from note editor dialog`() {
        //Given we have one note saved and three folders saved and we're in the basic list state
        val noteID = repository.add(
            RoomNoteBuilder().setTitle("homework 1").setContent("homework 1 content").also {
                it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build()))
            }.build()
        )
        viewModel._eventListener.value = NoteListEvent.AttemptToAddNewFolder(
            RoomFolderBuilder().setName("Homework").setColor("Green").build()
        )
        viewModel._eventListener.value = NoteListEvent.AttemptToAddNewFolder(
            RoomFolderBuilder().setName("Chores").setColor("Blue").build()
        )
        viewModel._eventListener.value = NoteListEvent.AttemptToAddNewFolder(
            RoomFolderBuilder().setName("Other Stuff").setColor("Red").build()
        )
        //When we open the edit note details button and try to add that note to two folders
        viewModel._eventListener.value = NoteListEvent.EditNoteButtonClicked(1)
        viewModel._eventListener.value = NoteListEvent.ChangeNoteFolderMembershipButtonClicked(
            noteID, listOf(2, 4)
        )

        //Then assert the note is a part of the two new folders and all notes, and is not a part of the chores folder.
        viewModel._eventListener.value = NoteListEvent.PageLoaded
        assertEquals(1, (viewModel.uiState.value as NoteListViewState.ShowBasicListState).notes.size)
        assertEquals(repository.getDefaultFolder().name, (viewModel.uiState.value as NoteListViewState.ShowBasicListState).folder.name)
        viewModel._eventListener.value = NoteListEvent.FolderTrayButtonClicked
        viewModel._eventListener.value = NoteListEvent.ChangeFolderButtonClicked(2)
        assertEquals(1, (viewModel.uiState.value as NoteListViewState.ShowBasicListState).notes.size)
        assertEquals("Homework", (viewModel.uiState.value as NoteListViewState.ShowBasicListState).folder.name)
        viewModel._eventListener.value = NoteListEvent.FolderTrayButtonClicked
        viewModel._eventListener.value = NoteListEvent.ChangeFolderButtonClicked(4)
        assertEquals(1, (viewModel.uiState.value as NoteListViewState.ShowBasicListState).notes.size)
        assertEquals("Other Stuff", (viewModel.uiState.value as NoteListViewState.ShowBasicListState).folder.name)
        viewModel._eventListener.value = NoteListEvent.ChangeFolderButtonClicked(3)
        assertEquals(0, (viewModel.uiState.value as NoteListViewState.ShowBasicListState).notes.size)
        assertEquals("Chores", (viewModel.uiState.value as NoteListViewState.ShowBasicListState).folder.name)
    }

    @Test
    fun `test remove note from folder from note editor dialog`() {
        //Given we have one note saved and two folders saved (the note belongs to only one of the folders)
        val noteID = repository.add(
            RoomNoteBuilder().setTitle("homework 1").setContent("homework 1 content").also {
                it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build()))
            }.build()
        )
        viewModel._eventListener.value = NoteListEvent.AttemptToAddNewFolder(
            RoomFolderBuilder().setName("Homework").setColor("Green").build()
        )
        viewModel._eventListener.value = NoteListEvent.AttemptToAddNewFolder(
            RoomFolderBuilder().setName("Chores").setColor("Blue").build()
        )
        repository.addNoteToFolder(repository.getNote(noteID), repository.getFolder(3))
        repository.addNoteToFolder(repository.getNote(noteID), repository.getFolder(1))


        //First assert that the note is in that folder
        viewModel._eventListener.value = NoteListEvent.ChangeFolderButtonClicked(2)
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowBasicListState)
        var state = viewModel.uiState.value as NoteListViewState.ShowBasicListState
        assertEquals("Homework", state.folder.name)
        assertEquals(0, state.notes.size)

        viewModel._eventListener.value = NoteListEvent.ChangeFolderButtonClicked(3)
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowBasicListState)
        state = viewModel.uiState.value as NoteListViewState.ShowBasicListState
        assertEquals("Chores", state.folder.name)
        assertEquals(1, state.notes.size)

        //Then when we delete that note from the folder
        viewModel._eventListener.value = NoteListEvent.EditNoteButtonClicked(1)
        viewModel._eventListener.value = NoteListEvent.ChangeNoteFolderMembershipButtonClicked(1, listOf())

        //Assert the note has been removed from the folder.
        viewModel._eventListener.value = NoteListEvent.ChangeFolderButtonClicked(3)
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowBasicListState)
        state = viewModel.uiState.value as NoteListViewState.ShowBasicListState
        assertEquals("Chores", state.folder.name)
        assertEquals(0, state.notes.size)

        //And assert it is still in the all notes folder
        viewModel._eventListener.value = NoteListEvent.ChangeFolderButtonClicked(1)
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowBasicListState)
        state = viewModel.uiState.value as NoteListViewState.ShowBasicListState
        assertEquals(repository.getDefaultFolder().name, state.folder.name)
        assertEquals(1, state.notes.size)
    }

    @Test
    fun `test change note title from note editor dialog`() {
        //Given we have one note saved and are in show basic list state.
        val noteID = repository.add(
            RoomNoteBuilder()
                .setTitle("Hello")
                .setContent("World")
                .also { it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build())) }
                .build()
        )
        viewModel._eventListener.value = NoteListEvent.PageLoaded

        //When we edit the notes title
        val newNoteBuilder = RoomNoteBuilder().clone(repository.getNote(noteID)).setTitle("Goodbye")
        viewModel._eventListener.value = NoteListEvent.AttemptToRenameNote(newNoteBuilder.build())

        //Then assert the note has changed title.
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowRenameNoteSuccessMessage)
        viewModel._eventListener.value = NoteListEvent.PageLoaded
        val state = viewModel.uiState.value as NoteListViewState.ShowBasicListState
        assertEquals(1, state.notes.size)
        assertEquals("World", state.notes[0].content)
        assertEquals("Goodbye", state.notes[0].title)
    }

    @Test
    fun `test change note title from note editor dialog fail case`() {
        //Given we have one note saved and are in show basic list state.
        val noteID = repository.add(
            RoomNoteBuilder()
                .setTitle("Hello")
                .setContent("World")
                .also { it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build())) }
                .build()
        )
        viewModel._eventListener.value = NoteListEvent.PageLoaded

        //When we edit the notes title
        val newNoteBuilder = RoomNoteBuilder().clone(repository.getNote(noteID)).setTitle("")
        viewModel._eventListener.value = NoteListEvent.AttemptToRenameNote(newNoteBuilder.build())

        //Then assert the note has changed title.
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowRenameNoteErrorMessage)
        viewModel._eventListener.value = NoteListEvent.PageLoaded
        val state = viewModel.uiState.value as NoteListViewState.ShowBasicListState
        assertEquals(1, state.notes.size)
        assertEquals("World", state.notes[0].content)
        assertEquals("Hello", state.notes[0].title)
    }

    @Test
    fun `test note editor is launched from note details editor options list`() {
        //Given we're in the basic list state with one note saved.
        repository.add(
            RoomNoteBuilder()
                .setTitle("Hello")
                .setContent("World")
                .also{it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build()))}
                .build()
        )

        //When we open the note details editor options list and click edit note.
        viewModel._eventListener.value = NoteListEvent.EditNoteButtonClicked(1)
        viewModel._eventListener.value = NoteListEvent.NoteClicked(1)

        //Then assert we're now in the note editor
        assertTrue(viewModel.uiState.value is NoteListViewState.OpenNoteEditor)
        val state = viewModel.uiState.value as NoteListViewState.OpenNoteEditor
        assertEquals(1, state.note.uid)
        assertEquals("Hello", state.note.title)
        assertEquals("World", state.note.content)
    }

    @Test
    fun `add new note button clicked`() {
        //Given we're in the basic list state
        viewModel._eventListener.value = NoteListEvent.PageLoaded
        //When we click on the add new note button
        viewModel._eventListener.value = NoteListEvent.AddNewNoteButtonClicked
        //Then assert we have a new empty note (it should be ok to have an empty note but it should have at least a title.)
        assertTrue(viewModel.uiState.value is NoteListViewState.OpenNoteEditor)
        val state = viewModel.uiState.value as NoteListViewState.OpenNoteEditor
        assertEquals(1, state.note.uid)
        assertEquals("New Note", state.note.title)
        assertEquals("", state.note.content)
        assertTrue(state.note.filePath.isNotEmpty())
    }

    @Test
    fun `add new note button clicked 3 times check note title is unique for each note`() {
        //Given we're in the basic list state
        viewModel._eventListener.value = NoteListEvent.PageLoaded

        //When we click on the add new note button 3 times to create 3 new notes
        viewModel._eventListener.value = NoteListEvent.AddNewNoteButtonClicked
        viewModel._eventListener.value = NoteListEvent.PageLoaded
        viewModel._eventListener.value = NoteListEvent.AddNewNoteButtonClicked
        viewModel._eventListener.value = NoteListEvent.PageLoaded
        viewModel._eventListener.value = NoteListEvent.AddNewNoteButtonClicked
        viewModel._eventListener.value = NoteListEvent.PageLoaded

        //Then assert we have 3 new empty notes
        val state = viewModel.uiState.value as NoteListViewState.ShowBasicListState
        assertEquals(3, state.notes.size)
        assertEquals("New Note", state.notes[0].title)
        assertEquals("New Note", state.notes[1].title)
        assertEquals("New Note", state.notes[2].title)
    }

    @Test
    fun `test search for note and cancel search`() {
        //Given we have 10 notes saved total and 6 in the homework folder
        //and we're in the homework folder.
        repository.add(RoomNoteBuilder().setTitle("1").setContent("1").also{it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build()))}.build())
        repository.add(RoomNoteBuilder().setTitle("2").setContent("2").also{it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build()))}.build())

        repository.add(RoomNoteBuilder().setTitle("Math 1").setContent("").also{it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build()))}.build())
        repository.add(RoomNoteBuilder().setTitle("English 1").setContent("").also{it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build()))}.build())
        repository.add(RoomNoteBuilder().setTitle("Math 2").setContent("").also{it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build()))}.build())
        repository.add(RoomNoteBuilder().setTitle("English 2").setContent("").also{it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build()))}.build())
        repository.add(RoomNoteBuilder().setTitle("English 3").setContent("").also{it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build()))}.build())
        repository.add(RoomNoteBuilder().setTitle("English 4").setContent("").also{it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build()))}.build())

        repository.add(RoomNoteBuilder().setTitle("9").setContent("9").also{it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build()))}.build())
        repository.add(RoomNoteBuilder().setTitle("10").setContent("10").also{it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build()))}.build())

        viewModel._eventListener.value = NoteListEvent.AttemptToAddNewFolder(
            RoomFolderBuilder().setName("Homework").setColor("Blue").build()
        )

        for (i in 3..8) {
            repository.addNoteToFolder(repository.getNote(i), repository.getFolder(2))
        }

        viewModel._eventListener.value = NoteListEvent.PageLoaded
        viewModel._eventListener.value = NoteListEvent.ChangeFolderButtonClicked(2)

        //When we search for a given note
        viewModel._eventListener.value = NoteListEvent.SearchCompleted("Math")

        //Then only notes with the search words in the title are returned
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowBasicListState)
        var state = viewModel.uiState.value as NoteListViewState.ShowBasicListState
        assertEquals(2, state.notes.size)
        assertEquals("Math 1", state.notes[0].title)
        assertEquals("Math 2", state.notes[1].title)

        //And When we cancel the search mode
        viewModel._eventListener.value = NoteListEvent.SearchClosed

        //Then we see all our 6 homework folder notes again.
        assertTrue(viewModel.uiState.value is NoteListViewState.ShowBasicListState)
        state = viewModel.uiState.value as NoteListViewState.ShowBasicListState
        assertEquals(6, state.notes.size)
        assertTrue("Math 1" in state.notes.map{it.title})
        assertTrue("Math 2" in state.notes.map{it.title})
        assertTrue("English 1" in state.notes.map{it.title})
        assertTrue("English 2" in state.notes.map{it.title})
        assertTrue("English 3" in state.notes.map{it.title})
        assertTrue("English 4" in state.notes.map{it.title})
    }


    @Test
    fun `test when renaming a note, note should still retain its previous folder memberships`() {
        //Given we have 3 notes saved, 2 of which are in the homework folder.
        val homeworkNote1Id = repository.add(RoomNoteBuilder().setTitle("Homework 1").setContent("Homework 1 Content").also{
            it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build()))
        }.build())
        val homeworkNote2Id = repository.add(RoomNoteBuilder().setTitle("Homework 2").setContent("Homework 2 Content").also{
            it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build()))
        }.build())
        val otherStuffId = repository.add(RoomNoteBuilder().setTitle("Other Stuff").setContent("Other Stuff Content").also{
            it.setFileName(repository.getCurrentOrGenerateNewFileName(it.build()))
        }.build())
        val homeworkFolderId = repository.add(RoomFolderBuilder().setName("Homework").setColor("Blue").build())
        repository.addNoteToFolder(repository.getNote(homeworkNote1Id), repository.getFolder(homeworkFolderId))
        repository.addNoteToFolder(repository.getNote(homeworkNote2Id), repository.getFolder(homeworkFolderId))
        viewModel._eventListener.value = NoteListEvent.PageLoaded


        //When we try to rename homework note 2
        viewModel._eventListener.value = NoteListEvent.AttemptToRenameNote(
            RoomNoteBuilder()
                .clone(repository.getNote(homeworkNote2Id))
                .setTitle("Second Homework Note")
                .build()
        )
        viewModel._eventListener.value = NoteListEvent.ChangeFolderButtonClicked(homeworkFolderId)

        //Assert its folder memberships are the same as before
        val state = viewModel.uiState.value as NoteListViewState.ShowBasicListState
        assertEquals(2, state.notes.size)
    }
}