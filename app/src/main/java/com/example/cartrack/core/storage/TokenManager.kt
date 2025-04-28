package com.example.cartrack.core.storage

import kotlinx.coroutines.flow.Flow

interface TokenManager {
    suspend fun saveToken(token: String)
    val tokenFlow: Flow<String?>
    suspend fun deleteToken()
}