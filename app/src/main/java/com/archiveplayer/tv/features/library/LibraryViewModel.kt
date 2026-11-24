package com.archiveplayer.tv.features.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.archiveplayer.tv.ArchivePlayerApplication
import com.archiveplayer.tv.data.local.EpisodeParser
import com.archiveplayer.tv.data.local.LibrarySection
import com.archiveplayer.tv.data.local.MediaItemEntity
import com.archiveplayer.tv.data.local.RecentlyWatchedItem
import com.archiveplayer.tv.data.local.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ArchivePlayerApplication

    val userProfile: Flow<UserProfileEntity?> = app.mediaRepository.getUserProfile()

    val recentlyWatched: Flow<List<RecentlyWatchedItem>> = app.mediaRepository.getRecentlyWatched().map { items ->
        items.mapNotNull { item ->
            val (season, episode) = EpisodeParser.parse(item.currentEpisodeFileName)
            val timeRemaining = (item.durationSeconds - item.watchProgressSeconds).coerceAtLeast(0)

            if (item.durationSeconds > 0) {
                RecentlyWatchedItem(
                    mediaItem = item,
                    seasonNumber = season,
                    episodeNumber = episode,
                    timeRemainingSeconds = timeRemaining
                )
            } else {
                null
            }
        }
    }

    val sections: Flow<List<LibrarySection>> = combine(
        app.mediaRepository.getRecentlyWatched(),
        app.mediaRepository.getAllMediaItems()
    ) { recentlyWatched, allItems ->
        buildSections(recentlyWatched, allItems)
    }

    private fun buildSections(recentlyWatched: List<MediaItemEntity>, items: List<MediaItemEntity>): List<LibrarySection> {
        val sections = mutableListOf<LibrarySection>()

        if (recentlyWatched.isNotEmpty()) {
            sections.add(
                LibrarySection(
                    categoryName = "Recently Watched",
                    items = recentlyWatched.sortedByDescending { it.lastWatched }
                )
            )
        }

        val grouped = items.groupBy { it.categoryName }

        val sortedKeys = grouped.keys.sortedWith(
            compareBy<String> { it == "Uncategorized" }
                .thenBy { it.lowercase() }
        )

        sortedKeys.forEach { key ->
            sections.add(
                LibrarySection(
                    categoryName = key,
                    items = grouped[key]!!.sortedBy { it.title.lowercase() }
                )
            )
        }

        return sections
    }
}