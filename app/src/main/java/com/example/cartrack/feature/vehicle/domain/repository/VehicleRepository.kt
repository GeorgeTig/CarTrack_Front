package com.example.cartrack.feature.vehicle.domain.repository

import com.example.cartrack.feature.vehicle.data.model.VehicleResponseDto

interface VehicleRepository {

    suspend fun getVehiclesByClientId(clientId: Int): Result<List<VehicleResponseDto>>

}