package com.example.cartrack.feature.vehicle.data.api

import com.example.cartrack.feature.vehicle.data.model.VehicleListResponse

interface VehicleApi {

    suspend fun getVehiclesByClientId(clientId: Int): VehicleListResponse
}