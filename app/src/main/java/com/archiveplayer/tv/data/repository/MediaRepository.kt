package com.archiveplayer.tv.data.repository

import com.archiveplayer.tv.data.local.MediaItemDao
import com.archiveplayer.tv.data.local.MediaItemEntity
import com.archiveplayer.tv.data.local.UserProfileDao
import com.archiveplayer.tv.data.local.UserProfileEntity
import com.archiveplayer.tv.data.local.WatchProgressDao
import com.archiveplayer.tv.data.local.WatchProgressEntity
import kotlinx.coroutines.flow.Flow

class MediaRepository(
    private val mediaItemDao: MediaItemDao,
    private val watchProgressDao: WatchProgressDao,
    private val userProfileDao: UserProfileDao
) {

    fun getAllMediaItems(): Flow<List<MediaItemEntity>> = mediaItemDao.getAllMediaItems()

    fun getMediaItemsByCategory(category: String): Flow<List<MediaItemEntity>> =
        mediaItemDao.getMediaItemsByCategory(category)

    fun getAllCategories(): Flow<List<String>> = mediaItemDao.getAllCategories()

    fun getRecentlyWatched(): Flow<List<WatchProgressEntity>> = watchProgressDao.getRecentlyWatched()

    fun getUserProfile(): Flow<UserProfileEntity?> = userProfileDao.getUserProfile()

    suspend fun getMediaItemById(id: Int): MediaItemEntity? = mediaItemDao.getMediaItemById(id)

    suspend fun addMediaItem(mediaItem: MediaItemEntity): Long = mediaItemDao.insert(mediaItem)

    suspend fun deleteMediaItem(id: Int) = mediaItemDao.deleteById(id)

    suspend fun updateWatchProgress(mediaItemId: Int, currentPositionMs: Long, durationMs: Long) {
        val progress = WatchProgressEntity(
            mediaItemId = mediaItemId,
            currentPositionMs = currentPositionMs,
            durationMs = durationMs,
            lastWatched = System.currentTimeMillis()
        )
        watchProgressDao.insertOrUpdate(progress)
    }

    suspend fun getWatchProgress(mediaItemId: Int): WatchProgressEntity? =
        watchProgressDao.getWatchProgress(mediaItemId)

    suspend fun saveProfileName(profileName: String) {
        val profile = UserProfileEntity(profileName = profileName)
        userProfileDao.insertOrUpdate(profile)
    }
}