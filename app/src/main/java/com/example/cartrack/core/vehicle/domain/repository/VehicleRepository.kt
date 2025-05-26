package com.example.cartrack.core.vehicle.domain.repository

import com.example.cartrack.core.vehicle.data.model.*

interface VehicleRepository {

    suspend fun getVehiclesByClientId(clientId: Int): Result<List<VehicleResponseDto>>
    suspend fun getVehicleEngine(vehicleId: Int): Result<VehicleEngineResponseDto>
    suspend fun getVehicleModel(vehicleId: Int): Result<VehicleModelResponseDto>
    suspend fun getVehicleInfo(vehicleId: Int): Result<VehicleInfoResponseDto>
    suspend fun getVehicleUsageStats(vehicleId: Int): Result<VehicleUsageStatsResponseDto>
    suspend fun getVehicleBody(vehicleId: Int): Result<VehicleBodyResponseDto>
    suspend fun getRemindersByVehicleId(vehicleId: Int): Result<List<ReminderResponseDto>>
    suspend fun updateReminder(request: ReminderRequestDto): Result<Unit>
    suspend fun updateReminderToDefault(reminderId: Int): Result<Unit>
    suspend fun updateReminderActiveStatus(reminderId: Int): Result<Unit>

}