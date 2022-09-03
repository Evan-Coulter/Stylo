package com.example.stylo.data.database

import androidx.room.*
import com.example.stylo.data.model.RoomNote

@Dao
interface NotesMetaDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(note: RoomNote)

    @Query("DELETE FROM roomnote WHERE uid = :id")
    fun delete(id: Int)

    @Query("SELECT * FROM roomnote")
    fun getAll(): List<RoomNote>
}