package com.example.stylo.data.model

class RoomFolderBuilder {
    private var uid: Int = 0
    private var name: String = "New Journal"
    private var color: String = "White"

    fun setUID(id: Int) : RoomFolderBuilder {
        this.uid = id
        return this
    }

    fun setName(name: String) : RoomFolderBuilder {
        this.name = name
        return this
    }

    fun setColor(color: String) : RoomFolderBuilder {
        this.color = color
        return this
    }

    fun build() : RoomFolder {
        return RoomFolder(uid, name, color)
    }
}