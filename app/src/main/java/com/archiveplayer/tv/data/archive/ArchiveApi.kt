package com.archiveplayer.tv.data.archive

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ArchiveApi {

    @GET("advancedsearch.php")
    suspend fun search(
        @Query("q") query: String,
        @Query("fl[]") fields: List<String> = listOf("identifier", "title", "mediatype", "description"),
        @Query("rows") rows: Int = 20,
        @Query("page") page: Int = 1,
        @Query("output") output: String = "json"
    ): Response<ArchiveSearchResponse>

    @GET("metadata/{identifier}")
    suspend fun getMetadata(@Path("identifier") identifier: String): Response<Map<String, Any>>
}