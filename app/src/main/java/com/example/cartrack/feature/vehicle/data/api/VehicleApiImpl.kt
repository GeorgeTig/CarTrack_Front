package com.example.cartrack.feature.vehicle.data.api

import com.example.cartrack.feature.vehicle.data.model.VehicleListResponse
import com.example.cartrack.feature.vehicle.data.model.VehicleResponseDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import javax.inject.Inject

class VehicleApiImpl @Inject constructor(
    private val client: HttpClient // HttpClient will be configured with Auth header
) : VehicleApi {

    // Adjust base URL if needed
    private val BASE_URL = "http://10.0.2.2:5098/api/vehicle"

    override suspend fun getVehiclesByClientId(clientId: Int): VehicleListResponse {
        // The Auth plugin configured in AppModules should automatically add the Bearer token
        // The backend [Authorize] attribute uses this token to identify the user/client
        return client.get("$BASE_URL/$clientId").body<VehicleListResponse>()
    }
}