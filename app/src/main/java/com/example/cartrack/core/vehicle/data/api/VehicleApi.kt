package com.example.cartrack.core.vehicle.data.api

import com.example.cartrack.core.vehicle.data.model.VehicleListResponse
import com.example.cartrack.core.vehicle.data.model.*
import io.ktor.client.statement.HttpResponse

interface VehicleApi {

    suspend fun getVehiclesByClientId(clientId: Int): VehicleListResponse
    suspend fun getVehicleEngine(vehicleId: Int): VehicleEngineResponseDto
    suspend fun getVehicleModel(vehicleId: Int): VehicleModelResponseDto
    suspend fun getVehicleInfo(vehicleId: Int): VehicleInfoResponseDto
    suspend fun getVehicleUsageStats(vehicleId: Int): VehicleUsageStatsResponseDto
    suspend fun getVehicleBody(vehicleId: Int): VehicleBodyResponseDto
    suspend fun getRemindersByVehicleId(vehicleId: Int): List<ReminderResponseDto>
    suspend fun updateReminder(request: ReminderRequestDto): HttpResponse
    suspend fun updateReminderToDefault(reminderId: Int): HttpResponse
    suspend fun updateReminderActiveStatus(reminderId: Int): HttpResponse // Or toggleReminderActive
}