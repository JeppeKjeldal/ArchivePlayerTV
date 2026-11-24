package com.archiveplayer.tv.data.repository

import com.archiveplayer.tv.data.local.MediaItemDao
import com.archiveplayer.tv.data.local.MediaItemEntity
import com.archiveplayer.tv.data.local.UserProfileDao
import com.archiveplayer.tv.data.local.UserProfileEntity
import com.archiveplayer.tv.data.local.WatchProgressDao
import kotlinx.coroutines.flow.Flow

class MediaRepository(
    private val mediaItemDao: MediaItemDao,
    private val watchProgressDao: WatchProgressDao,
    private val userProfileDao: UserProfileDao
) {
    fun getAllMediaItems(): Flow<List<MediaItemEntity>> {
        return mediaItemDao.getAllMediaItems()
    }

    fun getRecentlyWatched(): Flow<List<MediaItemEntity>> {
        return mediaItemDao.getRecentlyWatched()
    }

    suspend fun addMediaItem(item: MediaItemEntity) {
        mediaItemDao.insertMediaItem(item)
    }

    suspend fun getMediaItemByIdentifier(identifier: String): MediaItemEntity? {
        return mediaItemDao.getMediaItemByIdentifier(identifier)
    }

    suspend fun getMediaItemById(id: Int): MediaItemEntity? {
        return mediaItemDao.getMediaItemById(id)
    }

    suspend fun updateWatchProgress(id: Int, progressSeconds: Int, episodeFileName: String?, lastWatched: Long) {
        mediaItemDao.updateWatchProgress(id, progressSeconds, episodeFileName, lastWatched)
    }

    suspend fun saveProfileName(profileName: String) {
        val profile = UserProfileEntity(profileName = profileName)
        userProfileDao.insertUserProfile(profile)
    }

    fun getUserProfile(): Flow<UserProfileEntity?> {
        return userProfileDao.getUserProfile()
    }

    suspend fun updateDuration(id: Int, durationSeconds: Int) {
        mediaItemDao.updateDuration(id, durationSeconds)
    }
}