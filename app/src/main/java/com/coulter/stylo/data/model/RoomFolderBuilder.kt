package com.coulter.stylo.data.model

class RoomFolderBuilder {
    private var uid: Int = 0
    private var name: String = ""
    private var color: String = ""

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

    fun clone(folder: RoomFolder) : RoomFolderBuilder {
        return RoomFolderBuilder()
            .setUID(folder.uid)
            .setName(folder.name)
            .setColor(folder.color)
    }
}