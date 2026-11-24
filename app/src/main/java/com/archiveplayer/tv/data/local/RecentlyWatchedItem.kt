package com.archiveplayer.tv.data.local

data class RecentlyWatchedItem(
    val mediaItem: MediaItemEntity,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
    val timeRemainingSeconds: Int
)