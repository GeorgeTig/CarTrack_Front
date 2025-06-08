package com.example.cartrack.core.data.api

import com.example.cartrack.core.data.model.history.MaintenanceLogResponseDto
import com.example.cartrack.core.data.model.maintenance.MaintenanceSaveRequestDto
import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto
import com.example.cartrack.core.data.model.maintenance.ReminderUpdateRequestDto
import com.example.cartrack.core.data.model.vehicle.*
import io.ktor.client.statement.HttpResponse

interface VehicleApi {
    // Vehicle listing and creation
    suspend fun getVehiclesByClientId(clientId: Int): VehicleListResponseDto
    suspend fun saveVehicle(request: VehicleSaveRequestDto): HttpResponse
    suspend fun addMileageReading(vehicleId: Int, mileage: Double): HttpResponse

    // Detailed vehicle specs
    suspend fun getVehicleEngine(vehicleId: Int): VehicleEngineResponseDto
    suspend fun getVehicleModel(vehicleId: Int): VehicleModelResponseDto
    suspend fun getVehicleInfo(vehicleId: Int): VehicleInfoResponseDto
    suspend fun getVehicleBody(vehicleId: Int): VehicleBodyResponseDto
    suspend fun getDailyUsage(vehicleId: Int, timeZoneId: String): List<DailyUsageDto>

    // Reminders
    suspend fun getRemindersByVehicleId(vehicleId: Int): List<ReminderResponseDto>
    suspend fun getReminderById(reminderId: Int): ReminderResponseDto
    suspend fun updateReminder(request: ReminderUpdateRequestDto): HttpResponse
    suspend fun updateReminderToDefault(reminderId: Int): HttpResponse
    suspend fun updateReminderActiveStatus(reminderId: Int): HttpResponse

    // Maintenance logs
    suspend fun getMaintenanceHistory(vehicleId: Int): List<MaintenanceLogResponseDto>
    suspend fun addVehicleMaintenance(request: MaintenanceSaveRequestDto): HttpResponse
}