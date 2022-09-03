package com.example.stylo.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE

@Entity(foreignKeys = [
    ForeignKey(entity = RoomFolder::class, parentColumns = arrayOf("uid"), childColumns = arrayOf("folder"), onDelete = CASCADE)
])
data class CurrentRoomFolder @JvmOverloads constructor(
    val folder: String
)