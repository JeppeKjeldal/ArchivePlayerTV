package com.archiveplayer.tv.data.api

data class ApiLoginRequest(
    val email: String,
    val password: String,
    val rememberMe: Boolean = false
)

data class ApiRegisterRequest(
    val email: String,
    val password: String,
    val displayName: String
)

data class ApiAuthResponse(
    val success: Boolean,
    val message: String? = null,
    val displayName: String? = null
)

data class ApiProfileResponse(
    val email: String,
    val displayName: String
)