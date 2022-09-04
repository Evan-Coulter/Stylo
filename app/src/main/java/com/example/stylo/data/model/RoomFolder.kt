package com.example.stylo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RoomFolder constructor(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    val name: String,
    val color: String
)
