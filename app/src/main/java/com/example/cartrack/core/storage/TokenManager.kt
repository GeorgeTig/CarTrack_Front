package com.example.cartrack.core.storage

import kotlinx.coroutines.flow.Flow // <-- Import Flow

interface TokenManager {
    suspend fun saveToken(token: String)
    val tokenFlow: Flow<String?> // <-- ADD THIS LINE
    suspend fun deleteToken()
}