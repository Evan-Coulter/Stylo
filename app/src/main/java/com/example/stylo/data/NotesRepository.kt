package com.example.stylo.data

import com.example.stylo.data.database.NotesMetaDataDao
import com.example.stylo.data.exceptions.*
import com.example.stylo.data.fileaccess.FileAccessSource
import com.example.stylo.data.model.*

//Is actually 127 but keeping it safe.
private const val ANDROID_FILE_NAME_LENGTH_LIMIT = 125

class NotesRepository (private val dao: NotesMetaDataDao, private val fileAccessor: FileAccessSource) {

    fun add(note: RoomNote): Long {
        if (note.title.isEmpty() || note.content.isEmpty()) {
            throw NoteNotInitializedException()
        }
        if (note.filePath.isEmpty()) {
            throw FilePathNotSetException()
        }
        //Save as file
        saveToFile(note)
        //Save file meta data in database
        return dao.insert(note)
    }

    fun add(folder: RoomFolder): Long {
        if (folder.name.isEmpty() || folder.color.isEmpty()) {
            throw FolderNotInitializedException()
        }
        return dao.insert(folder)
    }

    fun addNoteToFolder(note: RoomNote, folder: RoomFolder): Long {
        if (!getAllNotes().contains(note)) {
            throw NoteNotFoundException()
        }
        if (!getAllFolders().contains(folder)) {
            throw FolderNotFoundException()
        }
        val belongsTo = BelongsToBuilder()
            .setFolder(folder.uid)
            .setNote(note.uid)
            .build()
        return dao.insert(belongsTo)
    }

    fun removeNoteFromFolder(note: RoomNote, folder: RoomFolder) {
        val belongsTo = dao.getAllBelongsTo().first { it.folder == folder.uid && it.note == folder.uid }
        dao.deleteBelongsTo(belongsTo.id)
    }

    fun delete(note: RoomNote) {
        if (!getAllNotes().contains(note)) {
            throw NoteNotFoundException()
        }
        dao.deleteNote(note.uid)
        fileAccessor.deleteFile(note.filePath)
    }

    fun delete(folder: RoomFolder) {
        if (!getAllFolders().contains(folder)) {
            throw FolderNotFoundException()
        }
        dao.deleteFolder(folder.uid)
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

    fun getAllFolders() : List<RoomFolder> {
        return dao.getAllFolders()
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