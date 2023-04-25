package com.coulter.stylo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.coulter.stylo.data.model.BelongsTo
import com.coulter.stylo.data.model.RoomFolder
import com.coulter.stylo.data.model.RoomNote

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
                ).allowMainThreadQueries()
                .build()
                instance = database
                return database
            }
        }
    }
}