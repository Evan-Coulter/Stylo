package com.example.stylo.data.fileaccess

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FileAccessSourceTest {
    private lateinit var fileAccessor: FileAccessSource

    @Before
    fun setupFileAccessor() {
        fileAccessor = FileAccessSource(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `test store and retrieve file`() {
        //Given one saved unique file
        fileAccessor.createFile("file_name", "This is the contents of the file")

        //Then we expect to be able to retrieve that file
        val fileContents = fileAccessor.getFileContents("file_name")
        assertEquals("This is the contents of the file", fileContents)
    }

    @Test
    fun `test store and delete files`() {
        //Given 3 saved files
        fileAccessor.createFile("my_file_1", "One")
        fileAccessor.createFile("my_file_2", "Two")
        fileAccessor.createFile("my_file_3", "Three")


        //When 2 are deleted
        fileAccessor.deleteFile("my_file_1")
        fileAccessor.deleteFile("my_file_3")

        //Then expect only 1 to remain
        val files = fileAccessor.getAllFilesNames()
        assertEquals(1, files.size)
        assertEquals("my_file_2", files.first())
        val fileContent = fileAccessor.getFileContents(files.first())
        assertEquals(fileContent, "Two")
    }
}