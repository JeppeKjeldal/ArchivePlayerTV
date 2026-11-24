package com.archiveplayer.tv.features.directadd

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.archiveplayer.tv.ArchivePlayerApplication
import com.archiveplayer.tv.data.local.MediaItemEntity
import com.archiveplayer.tv.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DirectAddPreview(
    val identifier: String,
    val title: String,
    val description: String?,
    val thumbnailUrl: String,
    val tmdbId: Int? = null,
    val tmdbMediaType: String? = null,
    val tmdbPosterUrl: String? = null
)

class DirectAddViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ArchivePlayerApplication

    private val _preview = MutableStateFlow(null as DirectAddPreview?)
    val preview: StateFlow<DirectAddPreview?> = _preview.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isFetchingTmdb = MutableStateFlow(false)
    val isFetchingTmdb: StateFlow<Boolean> = _isFetchingTmdb.asStateFlow()

    @Suppress("UNCHECKED_CAST")
    fun fetchMetadata(input: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _preview.value = null

            val identifier = input.trim()
                .removePrefix("https://archive.org/details/")
                .removePrefix("http://archive.org/details/")
                .removeSuffix("/")

            try {
                val response = RetrofitClient.archiveApi.getMetadata(identifier)
                if (response.isSuccessful) {
                    val metadata = response.body() ?: emptyMap()
                    val meta = metadata["metadata"] as? Map<String, Any> ?: emptyMap()

                    val title = (meta["title"] as? String) ?: identifier
                    val description = meta["description"] as? String
                    val archiveThumbnail = "https://archive.org/services/img/$identifier"

                    var preview = DirectAddPreview(
                        identifier = identifier,
                        title = title,
                        description = description,
                        thumbnailUrl = archiveThumbnail
                    )
                    _preview.value = preview

                    _isFetchingTmdb.value = true
                    try {
                        val tmdbMatch = app.tmdbService.findMatchAsync(title)
                        if (tmdbMatch != null) {
                            Log.d("DirectAddVM", "TMDB match found: ${tmdbMatch.title} (${tmdbMatch.mediaType})")

                            preview = preview.copy(
                                description = description ?: tmdbMatch.title,
                                thumbnailUrl = tmdbMatch.posterUrl ?: archiveThumbnail,
                                tmdbId = tmdbMatch.tmdbId,
                                tmdbMediaType = tmdbMatch.mediaType,
                                tmdbPosterUrl = tmdbMatch.posterUrl
                            )
                            _preview.value = preview
                        } else {
                            Log.d("DirectAddVM", "No TMDB match found, using Archive.org data")
                        }
                    } catch (e: Exception) {
                        Log.e("DirectAddVM", "TMDB fetch failed", e)
                    } finally {
                        _isFetchingTmdb.value = false
                    }
                }
            } catch (e: Exception) {
                Log.e("DirectAddViewModel", "Error fetching metadata", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveToLibrary(categoryName: String) {
        val currentPreview = _preview.value ?: return
        viewModelScope.launch {
            val finalCategory = categoryName.takeIf { it.isNotBlank() } ?: "Uncategorized"

            val entity = MediaItemEntity(
                archiveIdentifier = currentPreview.identifier,
                title = currentPreview.title,
                description = currentPreview.description,
                thumbnailUrl = currentPreview.thumbnailUrl,
                categoryName = finalCategory,
                tmdbId = currentPreview.tmdbId,
                tmdbMediaType = currentPreview.tmdbMediaType,
                tmdbPosterUrl = currentPreview.tmdbPosterUrl
            )

            app.mediaRepository.addMediaItem(entity)
            Log.d("DirectAddViewModel", "Saved: ${currentPreview.title} (TMDB ID: ${currentPreview.tmdbId})")
        }
    }
}