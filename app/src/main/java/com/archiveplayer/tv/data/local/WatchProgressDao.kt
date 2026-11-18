package com.archiveplayer.tv.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchProgressDao {

    @Query("SELECT * FROM watch_progress")
    fun getAllWatchProgress(): Flow<List<WatchProgressEntity>>

    @Query("SELECT * FROM watch_progress WHERE mediaItemId = :mediaItemId")
    suspend fun getWatchProgress(mediaItemId: Int): WatchProgressEntity?

    @Query("SELECT wp.* FROM watch_progress wp ORDER BY wp.lastWatched DESC LIMIT 10")
    fun getRecentlyWatched(): Flow<List<WatchProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(watchProgress: WatchProgressEntity)

    @Query("DELETE FROM watch_progress WHERE mediaItemId = :mediaItemId")
    suspend fun deleteByMediaItemId(mediaItemId: Int)
}