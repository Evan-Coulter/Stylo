package com.example.stylo.data

import com.example.stylo.data.database.NotesMetaDataDao
import com.example.stylo.data.fileaccess.FileAccessSource
import com.example.stylo.data.model.BelongsToBuilder
import com.example.stylo.data.model.RoomFolder
import com.example.stylo.data.model.RoomNote

//Is actually 127 but keeping it safe.
private const val ANDROID_FILE_NAME_LENGTH_LIMIT = 125

class NotesRepository (private val dao: NotesMetaDataDao, private val fileAccessor: FileAccessSource) {

    fun addNote(note: RoomNote) {
        //Save as file
        saveToFile(note)
        //Save file meta data in database
        dao.insert(note)
    }

    fun addNoteToFolder(note: RoomNote, folder: RoomFolder) {
        val belongsTo = BelongsToBuilder()
            .setFolder(folder.uid)
            .setNote(note.uid)
            .build()
        dao.insert(belongsTo)
    }

    fun delete(note: RoomNote) {
        dao.deleteNote(note.uid)
    }

    fun getAllNotes() : List<RoomNote>{
        return dao.getAllNotes()
    }

    private fun saveToFile(note: RoomNote) {
        fileAccessor.saveFile(note.title, note.content)
    }

    fun generateNewFileName(note: RoomNote) : String {
        val fileNames = fileAccessor.getAllFilesNames()
        val newFileName = CharArray(ANDROID_FILE_NAME_LENGTH_LIMIT)
        StringBuilder(note.title.filter { !it.isWhitespace() })
            .getChars(0, ANDROID_FILE_NAME_LENGTH_LIMIT, newFileName, 0)
        var counter = '1'
        do {
            newFileName[newFileName.size-1] = counter
            counter++
        } while(fileNames.contains(newFileName.toString()))
        return newFileName.toString()
    }
}