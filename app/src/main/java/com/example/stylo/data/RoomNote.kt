package com.example.stylo.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class RoomNote (
    @PrimaryKey val uid: Int = -1,
    var title: String = "New Journal",
    val filePath: String = "",
    val dateCreated: Date = Calendar.getInstance().time,
    var dateLastSaved: Date = Calendar.getInstance().time,
)