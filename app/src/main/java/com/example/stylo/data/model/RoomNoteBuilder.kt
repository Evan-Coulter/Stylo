package com.example.stylo.data.model

import java.util.*

const val DEFAULT_NEW_NOTE_TITLE = "New Journal"
const val DEFAULT_NEW_NOTE_CONTENT = ""
const val DEFAULT_NEW_NOTE_FILE_NAME = ""

class RoomNoteBuilder {
    var uid = 0
        private set
    var title = DEFAULT_NEW_NOTE_TITLE
        private set
    var content = DEFAULT_NEW_NOTE_CONTENT
        private set
    var fileName = DEFAULT_NEW_NOTE_FILE_NAME
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

    fun setFileName(filePath: String) : RoomNoteBuilder {
        this.fileName = filePath
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

    fun build() : RoomNote {
        return RoomNote(uid, title, fileName, dateCreated, dateLastModified, content)
    }


    fun clone(note: RoomNote) : RoomNoteBuilder {
        this.uid = note.uid
        this.content = note.content
        this.title = note.title
        this.fileName = note.filePath
        this.dateCreated = note.dateCreated
        this.dateLastModified = note.dateLastSaved
        return this
    }
}