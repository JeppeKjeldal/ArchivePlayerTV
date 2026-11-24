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
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

data class Episode(
    val fileName: String,
    val displayName: String,
    val seasonNumber: Int = 1,
    val episodeNumber: Int,
    val thumbnailUrl: String? = null,
    val description: String? = null,
    val durationSeconds: Int = 0  // NYT: Varighed i sekunder
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
                        val parsedEpisodes = videoFiles.mapIndexed { index, file ->
                            val fileName = file["name"] as String
                            // RET: Hent length fra metadata (i sekunder som en streng)
                            val durationSeconds = (file["length"] as? String)?.toFloatOrNull()?.toInt() ?: 0

                            Episode(
                                fileName = fileName,
                                displayName = "Part ${index + 1}",
                                episodeNumber = index + 1,
                                description = _description.value,
                                durationSeconds = durationSeconds
                            )
                        }
                        _episodes.value = parsedEpisodes

                        // RET: Gem varigheden for det første afsnit med det samme
                        val firstEpisode = parsedEpisodes.first()
                        if (mediaItemId != 0 && firstEpisode.durationSeconds > 0) {
                            app.mediaRepository.updateDuration(mediaItemId, firstEpisode.durationSeconds)
                        }

                        playEpisode(firstEpisode.fileName, identifier)
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

        // RET: Opdater varigheden i databasen når vi skifter afsnit
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