package com.example.stylo.data

import android.content.Context
import androidx.room.*

@Database(entities = [RoomNote::class, RoomFolder::class, BelongsTo::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class NotesMetaDataDatabase : RoomDatabase() {
    abstract fun notesMetaDataDao() : NotesMetaDataDao

    companion object {
        @Volatile
        private var instance: NotesMetaDataDatabase? = null

        fun getInstance(context: Context): NotesMetaDataDatabase {
            return instance ?: synchronized(this) {
                val database = Room.databaseBuilder(
                    context.applicationContext,
                    NotesMetaDataDatabase::class.java,
                    "notes_database"
                ).build()
                instance = database
                return database
            }
        }
    }
}