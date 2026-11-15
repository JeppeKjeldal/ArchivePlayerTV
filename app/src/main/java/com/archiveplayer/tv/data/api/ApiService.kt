package com.archiveplayer.tv.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: ApiLoginRequest): Response<ApiAuthResponse>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: ApiRegisterRequest): Response<ApiAuthResponse>

    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<ApiAuthResponse>

    @GET("api/v1/auth/profile")
    suspend fun getProfile(): Response<ApiProfileResponse>
}