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

        // 1. Recently Watched (altid først, hvis der er noget)
        if (recentlyWatched.isNotEmpty()) {
            sections.add(
                LibrarySection(
                    categoryName = "Recently Watched",
                    items = recentlyWatched.sortedBy { it.title.lowercase() }
                )
            )
        }

        // 2. Grupper ALLE items efter kategori (RET: Vi fjerner ikke længere items der er i recently watched)
        val grouped = items.groupBy { it.categoryName }

        val sortedKeys = grouped.keys.sortedWith(
            compareBy<String> { it == "Uncategorized" } // Uncategorized sidst
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