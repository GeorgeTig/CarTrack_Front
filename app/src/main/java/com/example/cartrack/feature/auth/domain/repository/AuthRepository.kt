package com.example.cartrack.feature.auth.domain.repository

import com.example.cartrack.feature.auth.data.model.UserLoginRequest
import com.example.cartrack.feature.auth.data.model.UserRegisterRequest
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(request: UserLoginRequest): Result<Unit>
    suspend fun register(request: UserRegisterRequest): Result<Unit>
    suspend fun logout()
    fun isLoggedIn(): Flow<Boolean>
    fun hasVehicles(): Flow<Boolean>
    suspend fun hasVehicles(clientId: Int): Result<Unit>
}