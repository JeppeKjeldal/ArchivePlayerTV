package com.archiveplayer.tv.features.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.archiveplayer.tv.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.util.regex.Pattern

data class Episode(
    val fileName: String,
    val displayName: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val thumbnailUrl: String? = null,
    val description: String? = null
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val _episodes = MutableStateFlow<List<Episode>>(emptyList())
    val episodes: StateFlow<List<Episode>> = _episodes.asStateFlow()

    private val _selectedSeason = MutableStateFlow(1)
    val selectedSeason: StateFlow<Int> = _selectedSeason.asStateFlow()

    private val _currentFileName = MutableStateFlow("")
    val currentEpisodeFileName: StateFlow<String> = _currentFileName.asStateFlow()

    private val _title = MutableStateFlow("Loading...")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow<String?>(null)
    val description: StateFlow<String?> = _description.asStateFlow()

    val exoPlayer = ExoPlayer.Builder(application).build()

    fun loadMedia(identifier: String) {
        viewModelScope.launch {
            _title.value = "Loading metadata..."

            try {
                val response = RetrofitClient.archiveApi.getMetadata(identifier)
                if (response.isSuccessful) {
                    val metadata = response.body() ?: emptyMap<String, Any>()

                    val meta = metadata["metadata"] as? Map<String, Any> ?: emptyMap()
                    _title.value = (meta["title"] as? String) ?: identifier
                    _description.value = (meta["description"] as? String) ?: "No description available."

                    val files = metadata["files"] as? List<Map<String, Any>> ?: emptyList()

                    // Filtrer og sorter videofiler alfabetisk
                    val videoFiles = files.filter { file ->
                        val name = file["name"] as? String ?: ""
                        name.endsWith(".mp4", ignoreCase = true) ||
                                name.endsWith(".mkv", ignoreCase = true) ||
                                name.endsWith(".avi", ignoreCase = true) ||
                                name.endsWith(".mov", ignoreCase = true) ||
                                name.endsWith(".webm", ignoreCase = true)
                    }.sortedBy { it["name"] as? String ?: "" }

                    if (videoFiles.isNotEmpty()) {
                        // Regex patterns til at parse S01E01 eller 1x01 fra filnavne (ligesom i C#)
                        val pattern = Pattern.compile("(?i).*S(\\d+).*E(\\d+).*")
                        val fallbackPattern = Pattern.compile("(?i).*(\\d+)x(\\d+).*")

                        val parsedEpisodes = videoFiles.mapIndexed { index, file ->
                            val fileName = file["name"] as String
                            var seasonNumber = 1
                            var episodeNumber = index + 1
                            var displayName = "Part ${index + 1}"

                            val matcher = pattern.matcher(fileName)
                            if (matcher.find()) {
                                seasonNumber = matcher.group(1)?.toIntOrNull() ?: 1
                                episodeNumber = matcher.group(2)?.toIntOrNull() ?: (index + 1)
                                displayName = "S${seasonNumber}E${episodeNumber}"
                            } else {
                                val fallbackMatcher = fallbackPattern.matcher(fileName)
                                if (fallbackMatcher.find()) {
                                    seasonNumber = fallbackMatcher.group(1)?.toIntOrNull() ?: 1
                                    episodeNumber = fallbackMatcher.group(2)?.toIntOrNull() ?: (index + 1)
                                    displayName = "S${seasonNumber}E${episodeNumber}"
                                }
                            }

                            Episode(
                                fileName = fileName,
                                displayName = displayName,
                                seasonNumber = seasonNumber,
                                episodeNumber = episodeNumber,
                                description = _description.value
                            )
                        }

                        _episodes.value = parsedEpisodes
                        _selectedSeason.value = parsedEpisodes.first().seasonNumber

                        playEpisode(parsedEpisodes.first().fileName, identifier)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _title.value = "Error loading media"
            }
        }
    }

    fun playEpisode(fileName: String, identifier: String) {
        _currentFileName.value = fileName
        // Kod hver del af stien separat, så '/' bevares, men specialtegn kodes
        val encodedFileName = fileName.split("/")
            .joinToString("/") { URLEncoder.encode(it, "UTF-8").replace("+", "%20") }

        val videoUrl = "https://archive.org/download/$identifier/$encodedFileName"

        val mediaItem = MediaItem.fromUri(videoUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun selectSeason(season: Int) {
        _selectedSeason.value = season
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }
}