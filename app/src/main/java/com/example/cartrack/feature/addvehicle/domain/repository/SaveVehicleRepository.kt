package com.example.cartrack.feature.addvehicle.domain.repository

import com.example.cartrack.feature.addvehicle.data.model.VehicleSaveRequestDto

interface SaveVehicleRepository {
    suspend fun saveVehicle(request: VehicleSaveRequestDto): Result<Unit>
}