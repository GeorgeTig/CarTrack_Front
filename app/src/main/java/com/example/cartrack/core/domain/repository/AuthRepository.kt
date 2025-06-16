package com.example.cartrack.core.domain.repository

import com.example.cartrack.core.data.model.auth.UserLoginRequestDto
import com.example.cartrack.core.data.model.auth.UserRegisterRequestDto
import com.example.cartrack.core.data.model.vehicle.VehicleResponseDto
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(request: UserLoginRequestDto): Result<Unit>
    suspend fun register(request: UserRegisterRequestDto): Result<Unit>
    suspend fun logout()

    // Încearcă să reîmprospăteze token-ul. Util pentru pornirea aplicației.
    suspend fun attemptSilentRefresh(): Result<Unit>

    // Expune starea de login ca un Flow.
    fun isLoggedIn(): Flow<Boolean>

    // Verifică dacă utilizatorul are vehicule (necesar după login).
    suspend fun hasVehicles(): Result<List<VehicleResponseDto>>
}