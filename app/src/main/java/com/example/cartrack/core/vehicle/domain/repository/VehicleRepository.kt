package com.example.cartrack.core.vehicle.domain.repository

import com.example.cartrack.core.vehicle.data.model.VehicleResponseDto

interface VehicleRepository {

    suspend fun getVehiclesByClientId(clientId: Int): Result<List<VehicleResponseDto>>

}