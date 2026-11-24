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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ArchivePlayerApplication

    val userProfile: Flow<UserProfileEntity?> = app.mediaRepository.getUserProfile()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

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
        app.mediaRepository.getAllMediaItems(),
        _searchQuery
    ) { recentlyWatched, allItems, query ->
        buildSections(recentlyWatched, allItems, query)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearch() {
        _isSearching.value = !_isSearching.value
        if (!_isSearching.value) {
            _searchQuery.value = ""
        }
    }

    fun closeSearch() {
        _isSearching.value = false
        _searchQuery.value = ""
    }

    private fun buildSections(
        recentlyWatched: List<MediaItemEntity>,
        items: List<MediaItemEntity>,
        query: String
    ): List<LibrarySection> {
        val sections = mutableListOf<LibrarySection>()
        val normalizedQuery = query.trim().lowercase()

        val filteredRecentlyWatched = if (normalizedQuery.isBlank()) {
            recentlyWatched
        } else {
            recentlyWatched.filter { matchesQuery(it, normalizedQuery) }
        }

        if (filteredRecentlyWatched.isNotEmpty()) {
            sections.add(
                LibrarySection(
                    categoryName = "Recently Watched",
                    items = filteredRecentlyWatched.sortedByDescending { it.lastWatched }
                )
            )
        }

        val filteredItems = if (normalizedQuery.isBlank()) {
            items
        } else {
            items.filter { matchesQuery(it, normalizedQuery) }
        }

        val grouped = filteredItems.groupBy { it.categoryName }

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

    private fun matchesQuery(item: MediaItemEntity, normalizedQuery: String): Boolean {
        return item.title.lowercase().contains(normalizedQuery) ||
                item.categoryName.lowercase().contains(normalizedQuery) ||
                (item.description?.lowercase()?.contains(normalizedQuery) == true)
    }
}