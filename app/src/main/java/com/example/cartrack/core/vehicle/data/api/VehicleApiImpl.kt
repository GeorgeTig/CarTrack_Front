package com.example.cartrack.core.vehicle.data.api

import com.example.cartrack.core.vehicle.data.model.VehicleListResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import javax.inject.Inject

class VehicleApiImpl @Inject constructor(
    private val client: HttpClient
) : VehicleApi {

    // Adjust base URL if needed
    private val BASE_URL = "http://10.0.2.2:5098/api/vehicle"

    override suspend fun getVehiclesByClientId(clientId: Int): VehicleListResponse {
        return client.get("$BASE_URL/$clientId").body<VehicleListResponse>()
    }
}