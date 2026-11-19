package com.archiveplayer.tv.features.add

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.archiveplayer.tv.ArchivePlayerApplication
import com.archiveplayer.tv.data.local.MediaItemEntity
import kotlinx.coroutines.launch

class AddToLibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ArchivePlayerApplication

    fun saveToLibrary(identifier: String, title: String, description: String?, thumbnailUrl: String, categoryName: String) {
        viewModelScope.launch {
            val finalCategory = categoryName.takeIf { it.isNotBlank() } ?: "Uncategorized"

            val entity = MediaItemEntity(
                archiveIdentifier = identifier,
                title = title,
                description = description,
                thumbnailUrl = thumbnailUrl,
                categoryName = finalCategory
            )

            app.mediaRepository.addMediaItem(entity)
            Log.d("AddToLibraryViewModel", "Saved: $title to category: $finalCategory")
        }
    }
}