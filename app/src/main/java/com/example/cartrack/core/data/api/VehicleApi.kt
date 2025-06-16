package com.example.cartrack.core.data.api

import com.example.cartrack.core.data.model.history.MaintenanceLogResponseDto
import com.example.cartrack.core.data.model.maintenance.CustomReminderRequestDto
import com.example.cartrack.core.data.model.maintenance.MaintenanceSaveRequestDto
import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto
import com.example.cartrack.core.data.model.maintenance.ReminderTypeResponseDto
import com.example.cartrack.core.data.model.maintenance.ReminderUpdateRequestDto
import com.example.cartrack.core.data.model.vehicle.*
import io.ktor.client.statement.HttpResponse

interface VehicleApi {
    suspend fun getVehiclesByClientId(): VehicleListResponseDto
    suspend fun saveVehicle(request: VehicleSaveRequestDto): HttpResponse
    suspend fun getVehicleEngine(vehicleId: Int): VehicleEngineResponseDto
    suspend fun getVehicleModel(vehicleId: Int): VehicleModelResponseDto
    suspend fun getVehicleInfo(vehicleId: Int): VehicleInfoResponseDto
    suspend fun getVehicleBody(vehicleId: Int): VehicleBodyResponseDto
    suspend fun getRemindersByVehicleId(vehicleId: Int): List<ReminderResponseDto>
    suspend fun getReminderById(reminderId: Int): ReminderResponseDto
    suspend fun updateReminder(request: ReminderUpdateRequestDto): HttpResponse
    suspend fun updateReminderActiveStatus(reminderId: Int): HttpResponse
    suspend fun addVehicleMaintenance(request: MaintenanceSaveRequestDto): HttpResponse
    suspend fun getMaintenanceHistory(vehicleId: Int): List<MaintenanceLogResponseDto>
    suspend fun getDailyUsage(vehicleId: Int, timeZoneId: String): List<DailyUsageDto>
    suspend fun addMileageReading(vehicleId: Int, mileage: Double): HttpResponse
    suspend fun deactivateVehicle(vehicleId: Int): HttpResponse
    suspend fun addCustomReminder(vehicleId: Int, request: CustomReminderRequestDto): HttpResponse
    suspend fun getAllReminderTypes(): List<ReminderTypeResponseDto>
    suspend fun deactivateCustomReminder(configId: Int): HttpResponse
    suspend fun resetReminderToDefault(configId: Int): HttpResponse
}