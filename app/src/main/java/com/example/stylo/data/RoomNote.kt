package com.example.stylo.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class RoomNote (
    @PrimaryKey(autoGenerate = true)
    var uid: Int = -1,
    var title: String = "New Journal",
    @Ignore var
    content: String = "",
    var filePath: String = "",
    var dateCreated: Date = Calendar.getInstance().time,
    var dateLastSaved: Date = Calendar.getInstance().time,
)