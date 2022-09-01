package com.example.stylo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RoomFolder(
    @PrimaryKey(autoGenerate = true)
    var uid: Int,
    var name: String,
    var color: String
)
