package com.example.cartrack.core.vehicle.data.api

import com.example.cartrack.core.vehicle.data.model.VehicleListResponse
import io.ktor.client.request.*
import javax.inject.Inject
import com.example.cartrack.core.vehicle.data.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

class VehicleApiImpl @Inject constructor(
    private val client: HttpClient
) : VehicleApi {

    // Adjust base URL if needed
    private val BASE_URL = "http://10.0.2.2:5098/api/vehicle"

    override suspend fun getVehiclesByClientId(clientId: Int): VehicleListResponse {
        return client.get("$BASE_URL/$clientId").body<VehicleListResponse>()
    }

    override suspend fun getVehicleEngine(vehicleId: Int): VehicleEngineResponseDto {
        return client.get("$BASE_URL/engine/$vehicleId") {
            contentType(ContentType.Application.Json)
        }.body()
    }

    override suspend fun getVehicleModel(vehicleId: Int): VehicleModelResponseDto {
        return client.get("$BASE_URL/model/$vehicleId") {
            contentType(ContentType.Application.Json)
        }.body()
    }

    override suspend fun getVehicleInfo(vehicleId: Int): VehicleInfoResponseDto {
        return client.get("$BASE_URL/info/$vehicleId") {
            contentType(ContentType.Application.Json)
        }.body()
    }

    override suspend fun getVehicleUsageStats(vehicleId: Int): VehicleUsageStatsResponseDto {
        return client.get("$BASE_URL/usage/$vehicleId") {
            contentType(ContentType.Application.Json)
        }.body()
    }

    override suspend fun getVehicleBody(vehicleId: Int): VehicleBodyResponseDto {
        return client.get("$BASE_URL/body/$vehicleId") {
            contentType(ContentType.Application.Json)
        }.body()
    }

    override suspend fun getRemindersByVehicleId(vehicleId: Int): List<ReminderResponseDto> {
        return client.get("$BASE_URL/reminders/$vehicleId") {
            contentType(ContentType.Application.Json)
        }.body()
    }

    override suspend fun updateReminder(request: ReminderRequestDto): HttpResponse {
        return client.post("$BASE_URL/update/reminder") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun updateReminderToDefault(reminderId: Int): HttpResponse {
        return client.post("$BASE_URL/update/reminder$reminderId/default") {
            contentType(ContentType.Application.Json)
        }
    }

    override suspend fun updateReminderActiveStatus(reminderId: Int): HttpResponse {
        return client.post("$BASE_URL/update/reminder$reminderId/active") {
            contentType(ContentType.Application.Json)
        }
    }
}