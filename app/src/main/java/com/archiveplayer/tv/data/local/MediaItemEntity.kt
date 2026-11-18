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
    val dateAdded: Long = System.currentTimeMillis()
)