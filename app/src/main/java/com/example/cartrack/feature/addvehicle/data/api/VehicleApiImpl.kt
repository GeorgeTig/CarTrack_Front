package com.example.cartrack.feature.addvehicle.data.api

import com.example.cartrack.feature.addvehicle.data.model.VehicleSaveRequestDto
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import javax.inject.Inject

class VehicleApiImpl @Inject constructor(
    private val client: HttpClient
) : VehicleApi {

    // Adjust BASE_URL if your API is hosted differently for this endpoint
    private val BASE_URL = "http://10.0.2.2:5098/api/vehicle"

    override suspend fun saveVehicle(request: VehicleSaveRequestDto) {
        // Ktor client post throws exceptions for non-2xx responses by default
        client.post("$BASE_URL/add") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        // No explicit return needed; success is indicated by absence of exception
    }
}
