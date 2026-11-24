package com.archiveplayer.tv.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItem(item: MediaItemEntity)

    @Update
    suspend fun updateMediaItem(item: MediaItemEntity)

    @Query("SELECT * FROM media_items ORDER BY categoryName ASC")
    fun getAllMediaItems(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE archiveIdentifier = :identifier LIMIT 1")
    suspend fun getMediaItemByIdentifier(identifier: String): MediaItemEntity?

    @Query("SELECT * FROM media_items WHERE id = :id LIMIT 1")
    suspend fun getMediaItemById(id: Int): MediaItemEntity?

    @Query("SELECT * FROM media_items WHERE lastWatched > 0 ORDER BY lastWatched DESC LIMIT 10")
    fun getRecentlyWatched(): Flow<List<MediaItemEntity>>

    @Query("UPDATE media_items SET watchProgressSeconds = :progressSeconds, currentEpisodeFileName = :episodeFileName, lastWatched = :lastWatched WHERE id = :id")
    suspend fun updateWatchProgress(id: Int, progressSeconds: Int, episodeFileName: String?, lastWatched: Long)

    @Query("UPDATE media_items SET durationSeconds = :durationSeconds WHERE id = :id")
    suspend fun updateDuration(id: Int, durationSeconds: Int)

    @Query("UPDATE media_items SET tmdbId = :tmdbId, tmdbMediaType = :tmdbMediaType, tmdbPosterUrl = :tmdbPosterUrl, description = :description, thumbnailUrl = :thumbnailUrl WHERE id = :id")
    suspend fun updateTmdbData(
        id: Int,
        tmdbId: Int?,
        tmdbMediaType: String?,
        tmdbPosterUrl: String?,
        description: String?,
        thumbnailUrl: String
    )
}