package com.example.stylo.data.database

import androidx.room.*
import com.example.stylo.data.model.BelongsTo
import com.example.stylo.data.model.RoomFolder
import com.example.stylo.data.model.RoomNote

@Dao
interface NotesMetaDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(note: RoomNote): Long

    @Query("DELETE FROM roomnote WHERE uid = :id")
    fun deleteNote(id: Int)

    @Query("SELECT * FROM roomnote")
    fun getAllNotes(): List<RoomNote>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(folder: RoomFolder): Long

    @Query("DELETE FROM roomfolder WHERE uid = :id")
    fun deleteFolder(id: Int)

    @Query("SELECT * FROM roomfolder")
    fun getAllFolders(): List<RoomFolder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(belongsTo: BelongsTo): Long

    @Query("DELETE FROM belongsto WHERE id = :id")
    fun deleteBelongsTo(id: Int)

    @Query("SELECT * FROM belongsto")
    fun getAllBelongsTo() : List<BelongsTo>
}