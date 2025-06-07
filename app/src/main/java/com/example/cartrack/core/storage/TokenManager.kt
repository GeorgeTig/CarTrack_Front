package com.example.cartrack.core.storage

import kotlinx.coroutines.flow.Flow

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String
)

interface TokenManager {
    suspend fun saveTokens(tokens: AuthTokens)
    suspend fun getTokens(): AuthTokens?
    suspend fun deleteTokens()

    val accessTokenFlow: Flow<String?>
    val refreshTokenFlow: Flow<String?>
}