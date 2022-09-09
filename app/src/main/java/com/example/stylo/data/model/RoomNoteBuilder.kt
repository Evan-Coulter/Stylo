package com.example.stylo.data.model

import java.util.*

const val DEFAULT_NEW_NOTE_TITLE = "New Journal"
const val DEFAULT_NEW_NOTE_CONTENT = ""
const val DEFAULT_NEW_NOTE_FILE_PATH = "new_file"

class RoomNoteBuilder {
    var uid = 0
        private set
    var title = DEFAULT_NEW_NOTE_TITLE
        private set
    var content = DEFAULT_NEW_NOTE_CONTENT
        private set
    var filePath = DEFAULT_NEW_NOTE_FILE_PATH
        private set
    var dateCreated: Date = Calendar.getInstance().time
        private set
    var dateLastModified: Date = Calendar.getInstance().time
        private set

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