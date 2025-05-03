package com.example.cartrack.feature.addvehicle.data.api

import com.example.cartrack.feature.addvehicle.data.model.VehicleSaveRequestDto

interface VehicleApi {
    suspend fun saveVehicle(request: VehicleSaveRequestDto)
}