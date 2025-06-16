package com.example.cartrack.core.domain.repository

import com.example.cartrack.core.data.model.auth.UserLoginRequestDto
import com.example.cartrack.core.data.model.auth.UserRegisterRequestDto
import com.example.cartrack.core.data.model.vehicle.VehicleResponseDto
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(request: UserLoginRequestDto): Result<Unit>
    suspend fun register(request: UserRegisterRequestDto): Result<Unit>
    suspend fun logout()

    suspend fun attemptSilentRefresh(): Result<Unit>
    fun isLoggedIn(): Flow<Boolean>
    suspend fun hasVehicles(): Result<List<VehicleResponseDto>>
}