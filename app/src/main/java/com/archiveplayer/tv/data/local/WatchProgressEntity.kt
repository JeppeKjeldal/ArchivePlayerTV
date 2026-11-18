package com.archiveplayer.tv.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_progress")
data class WatchProgressEntity(
    @PrimaryKey
    val mediaItemId: Int,
    val currentPositionMs: Long,
    val durationMs: Long,
    val lastWatched: Long = System.currentTimeMillis()
)