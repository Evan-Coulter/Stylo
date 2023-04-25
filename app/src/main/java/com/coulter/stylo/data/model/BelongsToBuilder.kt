package com.coulter.stylo.data.model

class BelongsToBuilder {
    private var id: Int = 0
    private var note: Int = 0
    private var folder: Int = 0

    fun setID(id: Int) : BelongsToBuilder {
        this.id = id
        return this
    }

    fun setNote(note: Int) : BelongsToBuilder {
        this.note = note
        return this
    }

    fun setFolder(folder: Int) : BelongsToBuilder {
        this.folder = folder
        return this
    }

    fun build() : BelongsTo {
        return BelongsTo(id, note, folder)
    }
}