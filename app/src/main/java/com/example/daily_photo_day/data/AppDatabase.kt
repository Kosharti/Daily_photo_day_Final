package com.example.daily_photo_day.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.daily_photo_day.data.dao.PhotoPostDao
import com.example.daily_photo_day.data.entity.PhotoPost

@Database(
    entities = [PhotoPost::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun photoPostDao(): PhotoPostDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "photo_diary_database"
                )
                .fallbackToDestructiveMigration() // для тестирования
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}