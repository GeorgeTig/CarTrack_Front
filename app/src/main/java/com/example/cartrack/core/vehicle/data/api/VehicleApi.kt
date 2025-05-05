package com.example.cartrack.core.vehicle.data.api

import com.example.cartrack.core.vehicle.data.model.VehicleListResponse

interface VehicleApi {

    suspend fun getVehiclesByClientId(clientId: Int): VehicleListResponse
}