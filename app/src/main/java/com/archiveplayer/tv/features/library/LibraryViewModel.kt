package com.archiveplayer.tv.features.library

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.archiveplayer.tv.ArchivePlayerApplication
import com.archiveplayer.tv.data.local.LibrarySection
import com.archiveplayer.tv.data.local.MediaItemEntity
import com.archiveplayer.tv.data.local.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ArchivePlayerApplication

    val userProfile: Flow<UserProfileEntity?> = app.mediaRepository.getUserProfile()

    val sections: Flow<List<LibrarySection>> = app.mediaRepository.getAllMediaItems()
        .map { items -> buildSections(items) }

    private fun buildSections(items: List<MediaItemEntity>): List<LibrarySection> {
        val grouped = items.groupBy { it.categoryName }

        val sortedKeys = grouped.keys.sortedWith(
            compareBy<String> { it != "Recently Watched" }
                .thenByDescending { it == "Uncategorized" }
                .thenBy { it.lowercase() }
        )

        return sortedKeys.map { key ->
            LibrarySection(
                categoryName = key,
                items = grouped[key]!!.sortedBy { it.title.lowercase() }
            )
        }
    }
}