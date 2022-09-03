package com.example.stylo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RoomFolder @JvmOverloads constructor(
    @PrimaryKey(autoGenerate = true)
    var uid: Int,
    var name: String,
    var color: String
)
