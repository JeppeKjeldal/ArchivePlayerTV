package com.archiveplayer.tv.data.tmdb

import android.util.Log
import com.archiveplayer.tv.ArchivePlayerApplication
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

class TmdbService(private val app: ArchivePlayerApplication) {

    private val json = Json { ignoreUnknownKeys = true }
    private val contentType = "application/json".toMediaType()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .client(OkHttpClient.Builder().build())
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()

    private val tmdbApi: TmdbApi = retrofit.create(TmdbApi::class.java)
    private val imageBaseUrl = "https://image.tmdb.org/t/p/w500"

    // Henter API-nøglen dynamisk fra SettingsManager
    private val apiKey: String?
        get() = app.settingsManager.tmdbApiKey

    suspend fun findMatchAsync(title: String): TmdbMatchResult? {
        val key = apiKey
        if (key.isNullOrBlank()) {
            Log.w("TMDB", "API Key not configured - skipping search")
            return null
        }

        val cleanedTitle = TitleCleaner.cleanForSearch(title)
        if (cleanedTitle.isBlank()) return null

        // Prøv TV først
        val tvResult = searchAsync(cleanedTitle, "tv", key)
        if (tvResult != null) return tvResult

        // Fald tilbage til Movie
        return searchAsync(cleanedTitle, "movie", key)
    }

    private suspend fun searchAsync(query: String, mediaType: String, key: String): TmdbMatchResult? {
        return try {
            val response = if (mediaType == "tv") {
                tmdbApi.searchTv(key, query)
            } else {
                tmdbApi.searchMovie(key, query)
            }

            val bestMatch = response.results.firstOrNull() ?: return null

            val posterUrl = if (!bestMatch.posterPath.isNullOrEmpty()) {
                "$imageBaseUrl${bestMatch.posterPath}"
            } else {
                null
            }

            TmdbMatchResult(
                tmdbId = bestMatch.id,
                title = bestMatch.name ?: bestMatch.title ?: query,
                posterUrl = posterUrl,
                mediaType = mediaType
            )
        } catch (e: Exception) {
            Log.e("TMDB", "Search failed for $query", e)
            null
        }
    }

    suspend fun fetchEpisodesAsync(tmdbId: Int): List<TmdbEpisodeInfo> {
        val key = apiKey ?: return emptyList()

        val episodes = mutableListOf<TmdbEpisodeInfo>()

        try {
            val show = tmdbApi.getTvDetails(tmdbId, key)

            for (season in show.seasons) {
                if (season.seasonNumber > 0) {
                    try {
                        val seasonDetails = tmdbApi.getSeasonDetails(tmdbId, season.seasonNumber, key)

                        for (ep in seasonDetails.episodes) {
                            val stillUrl = if (!ep.stillPath.isNullOrEmpty()) {
                                "$imageBaseUrl${ep.stillPath}"
                            } else {
                                null
                            }

                            episodes.add(
                                TmdbEpisodeInfo(
                                    seasonNumber = season.seasonNumber,
                                    episodeNumber = ep.episodeNumber,
                                    name = ep.name,
                                    stillUrl = stillUrl,
                                    overview = ep.overview
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.w("TMDB", "Failed to fetch season ${season.seasonNumber} for tmdbId: $tmdbId", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("TMDB", "Failed to fetch episodes for tmdbId: $tmdbId", e)
        }

        return episodes
    }
}