package com.archiveplayer.tv.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MediaItemEntity::class,
        UserProfileEntity::class,
        WatchProgressEntity::class  // (hvis du har denne)
    ],
    version = 2,  // RET: Opdater version fra 1 til 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaItemDao(): MediaItemDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun watchProgressDao(): WatchProgressDao  // (hvis du har denne)

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "archive_player_database"
                )
                    .fallbackToDestructiveMigration()  // NYT: Nulstil DB ved schema-ændring
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}