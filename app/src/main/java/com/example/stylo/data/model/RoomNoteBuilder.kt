package com.example.stylo.data.model

import java.util.*

const val DEFAULT_NEW_NOTE_TITLE = "New Journal"

class RoomNoteBuilder {
    private var uid = 0
    private var title = DEFAULT_NEW_NOTE_TITLE
    private var content = ""
    private var filePath = ""
    private var dateCreated = Calendar.getInstance().time
    private var dateLastModified = Calendar.getInstance().time

    fun setUID(id: Int) : RoomNoteBuilder {
        this.uid = id
        return this
    }

    fun setTitle(title: String) : RoomNoteBuilder {
        this.title = title
        return this
    }

    fun setContent(content: String) : RoomNoteBuilder {
        this.content = content
        return this
    }

    fun setFilePath(filePath: String) : RoomNoteBuilder {
        this.filePath = filePath
        return this
    }

    fun setDateCreated(date: Date) : RoomNoteBuilder {
        this.dateCreated = date
        return this
    }

    fun setDateLastModified(date: Date) : RoomNoteBuilder {
        this.dateLastModified = date
        return this
    }

    fun build() : RoomNote{
        return RoomNote(uid, title, filePath, dateCreated, dateLastModified, content)
    }
}