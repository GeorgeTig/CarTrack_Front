package com.example.cartrack.feature.vehicle.domain.repository

import com.example.cartrack.feature.vehicle.data.model.VehicleResponseDto
import kotlinx.coroutines.flow.Flow // Optional: If you want vehicles as a flow

interface VehicleRepository {
    // Simple suspend function returning Result
    suspend fun getVehiclesByClientId(clientId: Int): Result<List<VehicleResponseDto>>

    // Optional: If you want to observe vehicles (e.g., for potential updates)
    // fun getMyVehiclesFlow(): Flow<Result<List<VehicleResponseDto>>>
}