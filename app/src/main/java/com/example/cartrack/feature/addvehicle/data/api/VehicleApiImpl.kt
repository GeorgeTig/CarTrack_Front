package com.example.cartrack.feature.addvehicle.data.api

import com.example.cartrack.core.di.AuthenticatedHttpClient
import com.example.cartrack.feature.addvehicle.data.model.VehicleSaveRequestDto
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import javax.inject.Inject

class VehicleApiImpl @Inject constructor(
    @AuthenticatedHttpClient private val client: HttpClient // ADAUGĂ CALIFICATIVUL AICI
) : VehicleApi { // Asigură-te că implementează interfața corectă

    private val BASE_URL = "http://10.0.2.2:5098/api/vehicle"

    override suspend fun saveVehicle(request: VehicleSaveRequestDto) {
        client.post("$BASE_URL/add") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}