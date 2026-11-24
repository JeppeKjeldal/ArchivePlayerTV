package com.archiveplayer.tv.features.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.archiveplayer.tv.ArchivePlayerApplication
import com.archiveplayer.tv.data.local.LibrarySection
import com.archiveplayer.tv.data.local.MediaItemEntity
import com.archiveplayer.tv.data.local.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ArchivePlayerApplication

    val userProfile: Flow<UserProfileEntity?> = app.mediaRepository.getUserProfile()

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
                    items = recentlyWatched.sortedBy { it.title.lowercase() }
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