package com.example.stylo.data

import com.example.stylo.data.database.NotesMetaDataDao
import com.example.stylo.data.exceptions.FilePathNotSetException
import com.example.stylo.data.fileaccess.FileAccessSource
import com.example.stylo.data.model.*

//Is actually 127 but keeping it safe.
private const val ANDROID_FILE_NAME_LENGTH_LIMIT = 125

class NotesRepository (private val dao: NotesMetaDataDao, private val fileAccessor: FileAccessSource) {

    fun addNote(note: RoomNote) {
        if (note.filePath.isEmpty()) {
            throw FilePathNotSetException()
        }
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
        fileAccessor.deleteFile(note.filePath)
    }

    fun getAllNotes() : List<RoomNote>{
        val notes = dao.getAllNotes().map { note ->
            val fileContent = fileAccessor.getFileContents(note.filePath)
            RoomNoteBuilder().clone(note)
                .setContent(fileContent)
                .build()
        }
        return notes
    }

    private fun saveToFile(note: RoomNote) {
        fileAccessor.saveFile(note.filePath, note.content)
    }

    fun getCurrentOrGenerateNewFileName(note: RoomNote) : String {
        return if (note.filePath == DEFAULT_NEW_NOTE_FILE_NAME) {
            val fileNames = fileAccessor.getAllFilesNames()
            val newFileName = StringBuilder(note.title)
            var counter = '1'
            newFileName.append(counter)
            do {
                newFileName[newFileName.length-1] = counter
                counter++
            } while(fileNames.contains(newFileName.toString()))
            newFileName.toString()
        } else {
            note.filePath
        }
    }
}