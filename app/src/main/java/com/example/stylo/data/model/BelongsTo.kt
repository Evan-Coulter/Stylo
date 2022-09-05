package com.example.stylo.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(foreignKeys = [
    ForeignKey(entity = RoomNote::class, parentColumns = arrayOf("uid"), childColumns = arrayOf("note"), onDelete = CASCADE),
    ForeignKey(entity = RoomFolder::class, parentColumns = arrayOf("uid"), childColumns = arrayOf("folder"))
])
data class BelongsTo constructor(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val note: Int,
    val folder: Int
)
