package com.example.stylo.data

class NotesRepository (private val dao: NotesMetaDataDao) {

    fun add(note: RoomNote, noteText: String) {
        saveToFile(note, noteText)
        dao.insert(note)
    }

    fun delete(note: RoomNote) {
        dao.delete(note.uid)
    }

    fun getAll() : List<RoomNote>{
        return dao.getAll()
    }

    private fun saveToFile(note: RoomNote, noteText: String) {
        //TODO: implement save.
    }

}