package com.archiveplayer.tv.features.library

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.archiveplayer.tv.ArchivePlayerApplication
import com.archiveplayer.tv.data.local.MediaItemEntity
import com.archiveplayer.tv.data.local.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ArchivePlayerApplication

    val userProfile: Flow<UserProfileEntity?> = app.mediaRepository.getUserProfile()

    val allMediaItems: Flow<List<MediaItemEntity>> = app.mediaRepository.getAllMediaItems()

    val categories: Flow<List<String>> = app.mediaRepository.getAllCategories()

    fun addTestData() {
        viewModelScope.launch {
            val testItems = listOf(
                MediaItemEntity(
                    archiveIdentifier = "spas-2012",
                    title = "Mick Øgendahl - Spas",
                    description = "En test film fra Archive.org",
                    thumbnailUrl = "https://archive.org/services/img/spas-2012",
                    categoryName = "Stand-Up Comedy"
                ),
                MediaItemEntity(
                    archiveIdentifier = "timon-og-pumba-dktale",
                    title = "Timon og Pumba (DK tale)",
                    description = "En test serie fra Archive.org",
                    thumbnailUrl = "https://archive.org/services/img/timon-og-pumba-dktale",
                    categoryName = "Disney"
                )
            )

            testItems.forEach { item ->
                app.mediaRepository.addMediaItem(item)
                Log.d("LibraryViewModel", "Added test item: ${item.title}")
            }
        }
    }
}