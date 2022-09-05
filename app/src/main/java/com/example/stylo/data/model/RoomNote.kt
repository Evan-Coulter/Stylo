package com.example.stylo.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class RoomNote @JvmOverloads constructor(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    val title: String,
    val filePath: String,
    val dateCreated: Date,
    val dateLastSaved: Date,
    @Ignore val content: String = ""
)