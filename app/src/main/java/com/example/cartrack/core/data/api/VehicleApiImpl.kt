package com.example.cartrack.core.data.api

import com.example.cartrack.core.data.model.history.MaintenanceLogResponseDto
import com.example.cartrack.core.di.AuthenticatedHttpClient
import com.example.cartrack.core.data.model.maintenance.MaintenanceSaveRequestDto
import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto
import com.example.cartrack.core.data.model.maintenance.ReminderUpdateRequestDto
import com.example.cartrack.core.data.model.vehicle.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class VehicleApiImpl @Inject constructor(
    @AuthenticatedHttpClient private val client: HttpClient
) : VehicleApi {

    private val BASE_URL = "http://10.0.2.2:5098/api/vehicle"

    override suspend fun getVehiclesByClientId(): VehicleListResponseDto {
        return client.get("$BASE_URL/all").body()
    }

    override suspend fun saveVehicle(request: VehicleSaveRequestDto): HttpResponse {
        return client.post("$BASE_URL/add") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun getVehicleEngine(vehicleId: Int): VehicleEngineResponseDto {
        return client.get("$BASE_URL/engine/$vehicleId").body()
    }

    override suspend fun getVehicleModel(vehicleId: Int): VehicleModelResponseDto {
        return client.get("$BASE_URL/model/$vehicleId").body()
    }

    override suspend fun getVehicleInfo(vehicleId: Int): VehicleInfoResponseDto {
        return client.get("$BASE_URL/info/$vehicleId").body()
    }

    override suspend fun getVehicleBody(vehicleId: Int): VehicleBodyResponseDto {
        return client.get("$BASE_URL/body/$vehicleId").body()
    }

    // --- AICI ESTE CORECȚIA PRINCIPALĂ ---
    override suspend fun getRemindersByVehicleId(vehicleId: Int): List<ReminderResponseDto> {
        // Ruta corectă este /api/vehicle/{id}/reminders
        return client.get("$BASE_URL/$vehicleId/reminders").body()
    }

    override suspend fun getReminderById(reminderId: Int): ReminderResponseDto {
        return client.get("$BASE_URL/reminders/get/$reminderId").body()
    }

    override suspend fun getDailyUsage(vehicleId: Int, timeZoneId: String): List<DailyUsageDto> {
        return client.get("$BASE_URL/$vehicleId/usage/daily") {
            parameter("timeZoneId", timeZoneId)
        }.body()
    }

    override suspend fun addMileageReading(vehicleId: Int, mileage: Double): HttpResponse {
        return client.post("$BASE_URL/$vehicleId/mileage-readings") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("odometerValue" to mileage))
        }
    }

    override suspend fun updateReminder(request: ReminderUpdateRequestDto): HttpResponse {
        return client.post("$BASE_URL/update/reminder") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun updateReminderToDefault(reminderId: Int): HttpResponse {
        return client.post("$BASE_URL/update/reminder/$reminderId/default")
    }

    override suspend fun updateReminderActiveStatus(reminderId: Int): HttpResponse {
        return client.post("$BASE_URL/update/reminder/$reminderId/active")
    }

    override suspend fun addVehicleMaintenance(request: MaintenanceSaveRequestDto): HttpResponse {
        return client.post("$BASE_URL/maintenance") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun getMaintenanceHistory(vehicleId: Int): List<MaintenanceLogResponseDto> {
        return client.get("$BASE_URL/$vehicleId/history/maintenance").body()
    }
}