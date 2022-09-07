package com.example.stylo.data

import com.example.stylo.data.database.NotesMetaDataDao
import com.example.stylo.data.fileaccess.FileAccessSource
import com.example.stylo.data.model.RoomFolder
import com.example.stylo.data.model.RoomNote

class NotesRepository (private val dao: NotesMetaDataDao, private val fileAccessor: FileAccessSource) {

    fun addNote(note: RoomNote) {
        saveToFile(note)
        dao.insert(note)
    }

    fun addNoteToFolder(note: RoomNote, folder: RoomFolder) {

    }

    fun delete(note: RoomNote) {
        dao.deleteNote(note.uid)
    }

    fun getAllNotes() : List<RoomNote>{
        return dao.getAllNotes()
    }

    private fun saveToFile(note: RoomNote) {

    }

}