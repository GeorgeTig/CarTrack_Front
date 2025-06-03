package com.example.cartrack.core.storage

import kotlinx.coroutines.flow.Flow

interface TokenManager {
    suspend fun saveTokens(tokens: AuthTokens)
    val accessTokenFlow: Flow<String?>
    val refreshTokenFlow: Flow<String?>
    suspend fun getTokens(): AuthTokens? // Pentru uz sincron în Ktor
    suspend fun deleteTokens()
}