package com.archiveplayer.tv.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val archiveIdentifier: String,
    val title: String,
    val description: String?,
    val thumbnailUrl: String,
    val categoryName: String,
    val watchProgressSeconds: Int = 0,
    val currentEpisodeFileName: String? = null,
    val lastWatched: Long = 0L,
    val durationSeconds: Int = 0,
    val tmdbId: Int? = null,
    val tmdbMediaType: String? = null,
    val tmdbPosterUrl: String? = null
)