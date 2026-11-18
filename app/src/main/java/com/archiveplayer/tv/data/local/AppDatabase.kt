package com.archiveplayer.tv.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MediaItemEntity::class,
        WatchProgressEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun mediaItemDao(): MediaItemDao
    abstract fun watchProgressDao(): WatchProgressDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "archive_player_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}