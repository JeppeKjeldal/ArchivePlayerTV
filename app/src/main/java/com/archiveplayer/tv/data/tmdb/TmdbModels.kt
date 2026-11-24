package com.archiveplayer.tv.data.tmdb

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbSearchResponse(
    val results: List<TmdbSearchResult> = emptyList()
)

@Serializable
data class TmdbSearchResult(
    val id: Int,
    val title: String? = null,
    val name: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("media_type") val mediaType: String? = null
)

@Serializable
data class TmdbTvDetailsResponse(
    val seasons: List<TmdbSeasonSummary> = emptyList()
)

@Serializable
data class TmdbSeasonSummary(
    @SerialName("season_number") val seasonNumber: Int
)

@Serializable
data class TmdbSeasonDetailsResponse(
    val episodes: List<TmdbEpisodeResponse> = emptyList()
)

@Serializable
data class TmdbEpisodeResponse(
    @SerialName("episode_number") val episodeNumber: Int,
    val name: String? = null,
    @SerialName("still_path") val stillPath: String? = null,
    val overview: String? = null
)

// Vores egen samlede model til appen
data class TmdbMatchResult(
    val tmdbId: Int,
    val title: String,
    val posterUrl: String?,
    val mediaType: String
)

data class TmdbEpisodeInfo(
    val seasonNumber: Int,
    val episodeNumber: Int,
    val name: String?,
    val stillUrl: String?,
    val overview: String?
)