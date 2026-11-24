@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package com.archiveplayer.tv.features.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.archiveplayer.tv.ArchivePlayerApplication
import com.archiveplayer.tv.data.network.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.security.cert.X509Certificate
import java.util.regex.Pattern
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

data class Episode(
    val fileName: String,
    val displayName: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val thumbnailUrl: String? = null,
    val description: String? = null,
    val durationSeconds: Int = 0
)

@OptIn(UnstableApi::class)
class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ArchivePlayerApplication

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

    private var mediaItemId: Int = 0
    private var currentIdentifier: String = ""
    private var progressSaveJob: Job? = null

    private val lenientOkHttpClient by lazy {
        val trustAllCertificates = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf<TrustManager>(trustAllCertificates), java.security.SecureRandom())

        okhttp3.OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCertificates)
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    fun loadMedia(identifier: String) {
        viewModelScope.launch {
            _title.value = "Loading metadata..."

            val dbItem = app.mediaRepository.getMediaItemByIdentifier(identifier)
            if (dbItem != null) {
                mediaItemId = dbItem.id
            }

            try {
                val response = RetrofitClient.archiveApi.getMetadata(identifier)
                if (response.isSuccessful) {
                    val metadata = response.body() ?: emptyMap<String, Any>()
                    val meta = metadata["metadata"] as? Map<String, Any> ?: emptyMap()

                    _title.value = (meta["title"] as? String) ?: identifier
                    _description.value = (meta["description"] as? String) ?: "No description available."

                    val files = metadata["files"] as? List<Map<String, Any>> ?: emptyList()

                    val videoFiles = files.filter { file ->
                        val name = file["name"] as? String ?: ""
                        name.endsWith(".mp4", ignoreCase = true) ||
                                name.endsWith(".mkv", ignoreCase = true) ||
                                name.endsWith(".avi", ignoreCase = true) ||
                                name.endsWith(".mov", ignoreCase = true) ||
                                name.endsWith(".webm", ignoreCase = true)
                    }

                    if (videoFiles.isNotEmpty()) {
                        val primaryPattern = Pattern.compile("(?i).*[Ss](?:eason)?[\\s._-]*(\\d{1,2})[\\s._-]*[Ee](?:pisode)?[\\s._-]*(\\d{1,2}).*")
                        val fallbackPattern = Pattern.compile("(?i).*(\\d{1,2})x(\\d{1,2}).*")
                        val episodeOnlyPattern = Pattern.compile("(?i).*[Ee]pisode[\\s._-]*(\\d{1,2}).*")

                        val parsedEpisodes = videoFiles.mapIndexed { index, file ->
                            val fileName = file["name"] as String
                            val durationSeconds = (file["length"] as? String)?.toFloatOrNull()?.toInt() ?: 0

                            var seasonNumber = 1
                            var episodeNumber = index + 1
                            var displayName = "Part ${index + 1}"

                            val primaryMatcher = primaryPattern.matcher(fileName)
                            if (primaryMatcher.find()) {
                                seasonNumber = primaryMatcher.group(1)?.toIntOrNull() ?: 1
                                episodeNumber = primaryMatcher.group(2)?.toIntOrNull() ?: (index + 1)
                                displayName = "S${seasonNumber.toString().padStart(2, '0')}E${episodeNumber.toString().padStart(2, '0')}"
                            } else {
                                val fallbackMatcher = fallbackPattern.matcher(fileName)
                                if (fallbackMatcher.find()) {
                                    seasonNumber = fallbackMatcher.group(1)?.toIntOrNull() ?: 1
                                    episodeNumber = fallbackMatcher.group(2)?.toIntOrNull() ?: (index + 1)
                                    displayName = "S${seasonNumber.toString().padStart(2, '0')}E${episodeNumber.toString().padStart(2, '0')}"
                                } else {
                                    val episodeOnlyMatcher = episodeOnlyPattern.matcher(fileName)
                                    if (episodeOnlyMatcher.find()) {
                                        seasonNumber = 1
                                        episodeNumber = episodeOnlyMatcher.group(1)?.toIntOrNull() ?: (index + 1)
                                        displayName = "Episode ${episodeNumber}"
                                    }
                                }
                            }

                            Episode(
                                fileName = fileName,
                                displayName = displayName,
                                seasonNumber = seasonNumber,
                                episodeNumber = episodeNumber,
                                description = _description.value,
                                durationSeconds = durationSeconds
                            )
                        }

                        _episodes.value = parsedEpisodes

                        val episodeToStart = parsedEpisodes.find { it.fileName == dbItem?.currentEpisodeFileName }
                            ?: parsedEpisodes.first()

                        val startPositionMs = (dbItem?.watchProgressSeconds ?: 0) * 1000L

                        playEpisode(episodeToStart.fileName, identifier, startPositionMs)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _title.value = "Error loading media"
            }

            startProgressTracking()
        }
    }

    fun playEpisode(fileName: String, identifier: String, startPositionMs: Long = 0L) {
        _currentFileName.value = fileName
        currentIdentifier = identifier

        val encodedFileName = fileName.split("/")
            .joinToString("/") { URLEncoder.encode(it, "UTF-8").replace("+", "%20") }

        val videoUrl = "https://archive.org/download/$identifier/$encodedFileName"

        val dataSourceFactory = OkHttpDataSource.Factory(lenientOkHttpClient)
        val mediaItem = MediaItem.Builder().setUri(videoUrl).build()
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)

        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()

        if (startPositionMs > 0) {
            exoPlayer.seekTo(startPositionMs)
        }

        exoPlayer.play()

        viewModelScope.launch {
            val currentEpisode = _episodes.value.find { it.fileName == fileName }
            if (mediaItemId != 0 && currentEpisode != null && currentEpisode.durationSeconds > 0) {
                app.mediaRepository.updateDuration(mediaItemId, currentEpisode.durationSeconds)
            }
        }
    }

    fun selectSeason(season: Int) {
        _selectedSeason.value = season
    }

    private fun startProgressTracking() {
        progressSaveJob?.cancel()
        progressSaveJob = viewModelScope.launch {
            while (isActive) {
                delay(10000)
                saveProgress()
            }
        }
    }

    private fun saveProgress() {
        if (mediaItemId == 0) return

        val progressSeconds = (exoPlayer.currentPosition / 1000).toInt()
        val currentTime = System.currentTimeMillis()

        viewModelScope.launch {
            app.mediaRepository.updateWatchProgress(
                id = mediaItemId,
                progressSeconds = progressSeconds,
                episodeFileName = _currentFileName.value,
                lastWatched = currentTime
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        saveProgress()
        progressSaveJob?.cancel()
        exoPlayer.release()
    }
}