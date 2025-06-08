package com.example.cartrack.core.data.api

import com.example.cartrack.core.data.model.history.MaintenanceLogResponseDto
import com.example.cartrack.core.data.model.maintenance.MaintenanceSaveRequestDto
import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto
import com.example.cartrack.core.data.model.maintenance.ReminderUpdateRequestDto
import com.example.cartrack.core.data.model.vehicle.*
import com.example.cartrack.core.di.AuthenticatedHttpClient
import com.example.cartrack.core.storage.TokenManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class VehicleApiImpl @Inject constructor(
    @AuthenticatedHttpClient private val client: HttpClient,
    private val tokenManager: TokenManager
) : VehicleApi {

    private val BASE_URL = "http://10.0.2.2:5098/api/vehicle"

    // --- FUNCȚII AJUTĂTOARE ---
    // Funcție inline pentru request-uri GET care așteaptă un body deserializat.
    // `reified T` permite ca .body<T>() să funcționeze corect.
    private suspend inline fun <reified T> authorizedGet(path: String, crossinline block: HttpRequestBuilder.() -> Unit = {}): T {
        return client.get(path) {
            header("Authorization", "Bearer ${tokenManager.getTokens()?.accessToken}")
            block()
        }.body()
    }

    // Funcție inline pentru request-uri POST care returnează întregul HttpResponse.
    private suspend inline fun authorizedPost(path: String, crossinline block: HttpRequestBuilder.() -> Unit = {}): HttpResponse {
        return client.post(path) {
            header("Authorization", "Bearer ${tokenManager.getTokens()?.accessToken}")
            block()
        }
    }
    // ---

    override suspend fun getVehiclesByClientId(): VehicleListResponseDto =
        authorizedGet("$BASE_URL/all")

    override suspend fun saveVehicle(request: VehicleSaveRequestDto): HttpResponse =
        authorizedPost("$BASE_URL/add") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

    override suspend fun getVehicleEngine(vehicleId: Int): VehicleEngineResponseDto =
        authorizedGet("$BASE_URL/engine/$vehicleId")

    override suspend fun getVehicleModel(vehicleId: Int): VehicleModelResponseDto =
        authorizedGet("$BASE_URL/model/$vehicleId")

    override suspend fun getVehicleInfo(vehicleId: Int): VehicleInfoResponseDto =
        authorizedGet("$BASE_URL/info/$vehicleId")

    override suspend fun getVehicleBody(vehicleId: Int): VehicleBodyResponseDto =
        authorizedGet("$BASE_URL/body/$vehicleId")

    override suspend fun getRemindersByVehicleId(vehicleId: Int): List<ReminderResponseDto> =
        authorizedGet("$BASE_URL/$vehicleId/reminders")

    override suspend fun getReminderById(reminderId: Int): ReminderResponseDto =
        authorizedGet("$BASE_URL/reminders/get/$reminderId")

    override suspend fun getDailyUsage(vehicleId: Int, timeZoneId: String): List<DailyUsageDto> =
        authorizedGet("$BASE_URL/$vehicleId/usage/daily") {
            parameter("timeZoneId", timeZoneId)
        }

    override suspend fun addMileageReading(vehicleId: Int, mileage: Double): HttpResponse =
        authorizedPost("$BASE_URL/$vehicleId/mileage-readings") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("odometerValue" to mileage))
        }

    override suspend fun updateReminder(request: ReminderUpdateRequestDto): HttpResponse =
        authorizedPost("$BASE_URL/update/reminder") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

    override suspend fun updateReminderToDefault(reminderId: Int): HttpResponse =
        authorizedPost("$BASE_URL/update/reminder/$reminderId/default")

    override suspend fun updateReminderActiveStatus(reminderId: Int): HttpResponse =
        authorizedPost("$BASE_URL/update/reminder/$reminderId/active")

    override suspend fun addVehicleMaintenance(request: MaintenanceSaveRequestDto): HttpResponse =
        authorizedPost("$BASE_URL/maintenance") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

    override suspend fun getMaintenanceHistory(vehicleId: Int): List<MaintenanceLogResponseDto> =
        authorizedGet("$BASE_URL/$vehicleId/history/maintenance")
}