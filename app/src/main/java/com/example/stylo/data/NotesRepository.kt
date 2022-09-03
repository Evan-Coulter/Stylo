package com.example.stylo.data

import com.example.stylo.data.database.NotesMetaDataDao
import com.example.stylo.data.model.RoomNote
import java.util.*

class NotesRepository (private val dao: NotesMetaDataDao) {

    fun add(note: RoomNote) {
        saveToFile(note)
        dao.insert(note)
    }

    fun delete(note: RoomNote) {
        dao.delete(note.uid)
    }

    fun getAll() : List<RoomNote>{
        //return dao.getAll()
        return listOf(
            RoomNote(0, "Journal 1", "","/file", Calendar.getInstance().time, Calendar.getInstance().time),
            RoomNote(1, "Journal 2", "","/file", Calendar.getInstance().time, Calendar.getInstance().time),
            RoomNote(2, "Journal 3", "","/file", Calendar.getInstance().time, Calendar.getInstance().time)
        )
    }

    private fun saveToFile(note: RoomNote) {
        //TODO: implement save.
    }

}