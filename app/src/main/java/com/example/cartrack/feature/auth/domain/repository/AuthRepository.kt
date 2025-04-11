package com.example.cartrack.feature.auth.domain.repository

import com.example.cartrack.feature.auth.data.model.UserLoginRequest
import com.example.cartrack.feature.auth.data.model.UserRegisterRequest
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(request: UserLoginRequest): Result<Unit> // Return Unit on success
    suspend fun register(request: UserRegisterRequest): Result<Unit> // Return Unit on success
    suspend fun logout()
    fun isLoggedIn(): Flow<Boolean> // Flow to observe login status
}