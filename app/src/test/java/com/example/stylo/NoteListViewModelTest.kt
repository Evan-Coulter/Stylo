package com.example.stylo

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.stylo.data.NotesRepository
import com.example.stylo.data.database.NotesMetaDataDao
import com.example.stylo.data.database.NotesMetaDataDatabase
import com.example.stylo.data.fileaccess.FileAccessSource
import com.example.stylo.list.NoteListEvent
import com.example.stylo.list.NoteListViewModel
import com.example.stylo.list.NoteListViewState
import com.example.stylo.list.StateAndEventLog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class NoteListViewModelTest {
    @VisibleForTesting
    lateinit var notesMetaDataDao: NotesMetaDataDao
    private lateinit var database: NotesMetaDataDatabase
    private lateinit var repository: NotesRepository
    private lateinit var fileAccessor: FileAccessSource

    lateinit var viewModel: NoteListViewModel

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
        runTest {
            //Given note list just opened for the first time
            viewModel._eventListener.value = NoteListEvent.PageLoaded
            //Then we should have seen loading and done loading (basic list state) states
            StateAndEventLog.states.values.let {
                assertEquals(2, it.size)
                assertTrue(it.contains(NoteListViewState.LoadingState))
                var containsBasicListViewState = false
                it.forEach { state ->
                    if (state is NoteListViewState.ShowBasicListState) {
                        containsBasicListViewState = true
                    }
                }
                assertTrue(containsBasicListViewState)
            }
        }
    }

    @Test
    fun `test load initial full list state`() {}

    @Test
    fun `test help button pushed should show help dialog`() {}

    @Test
    fun `test logo button pushed should show cool effect`() {}

    @Test
    fun `test folder button with no folders saved pushed should open side tab with all notes folder`() {}

    @Test
    fun `test add folder button clicked should show create new folder dialog`() {}

    @Test
    fun `test search button pushed`() {}

    @Test
    fun `test card list switch view button pushed`() {}

    @Test
    fun `test note pushed`() {}

    @Test
    fun `test fab pushed`() {}
}