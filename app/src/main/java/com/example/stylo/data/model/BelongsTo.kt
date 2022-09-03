package com.example.stylo.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(foreignKeys = [
    ForeignKey(entity = RoomNote::class, parentColumns = arrayOf("uid"), childColumns = arrayOf("note"), onDelete = CASCADE),
    ForeignKey(entity = RoomFolder::class, parentColumns = arrayOf("uid"), childColumns = arrayOf("folder"))
])
data class BelongsTo @JvmOverloads constructor(
    @PrimaryKey(autoGenerate = true) var id: Int,
    var note: String,
    var folder: String
)
