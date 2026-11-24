package com.archiveplayer.tv.data.tmdb

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {
    @GET("search/tv")
    suspend fun searchTv(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbSearchResponse

    @GET("search/movie")
    suspend fun searchMovie(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbSearchResponse

    @GET("tv/{tvId}")
    suspend fun getTvDetails(
        @Path("tvId") tvId: Int,
        @Query("api_key") apiKey: String
    ): TmdbTvDetailsResponse

    @GET("tv/{tvId}/season/{seasonNumber}")
    suspend fun getSeasonDetails(
        @Path("tvId") tvId: Int,
        @Path("seasonNumber") seasonNumber: Int,
        @Query("api_key") apiKey: String
    ): TmdbSeasonDetailsResponse
}