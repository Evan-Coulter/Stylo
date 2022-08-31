package com.example.stylo.data

import androidx.room.*

@Dao
interface NotesMetaDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(note: RoomNote)

    @Query("DELETE FROM roomnote WHERE uid = :id")
    fun delete(id: Int)

    @Query("SELECT * FROM roomnote")
    fun getAll(): List<RoomNote>
}