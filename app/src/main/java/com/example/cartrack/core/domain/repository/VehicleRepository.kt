package com.example.cartrack.core.domain.repository

import com.example.cartrack.core.data.model.history.MaintenanceLogResponseDto
import com.example.cartrack.core.data.model.maintenance.CustomReminderRequestDto
import com.example.cartrack.core.data.model.maintenance.MaintenanceSaveRequestDto
import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto
import com.example.cartrack.core.data.model.maintenance.ReminderTypeResponseDto
import com.example.cartrack.core.data.model.maintenance.ReminderUpdateRequestDto
import com.example.cartrack.core.data.model.vehicle.*

interface VehicleRepository {
    suspend fun getVehiclesByClientId(): Result<List<VehicleResponseDto>>
    suspend fun saveVehicle(request: VehicleSaveRequestDto): Result<Unit>
    suspend fun addMileageReading(vehicleId: Int, mileage: Double): Result<Unit>

    suspend fun getVehicleEngine(vehicleId: Int): Result<VehicleEngineResponseDto>
    suspend fun getVehicleModel(vehicleId: Int): Result<VehicleModelResponseDto>
    suspend fun getVehicleInfo(vehicleId: Int): Result<VehicleInfoResponseDto>
    suspend fun getVehicleBody(vehicleId: Int): Result<VehicleBodyResponseDto>
    suspend fun getDailyUsage(vehicleId: Int, timeZoneId: String): Result<List<DailyUsageDto>>

    suspend fun getRemindersByVehicleId(vehicleId: Int): Result<List<ReminderResponseDto>>
    suspend fun getReminderById(reminderId: Int): Result<ReminderResponseDto>
    suspend fun updateReminder(request: ReminderUpdateRequestDto): Result<Unit>
    suspend fun updateReminderActiveStatus(reminderId: Int): Result<Unit>

    suspend fun getMaintenanceHistory(vehicleId: Int): Result<List<MaintenanceLogResponseDto>>
    suspend fun saveVehicleMaintenance(request: MaintenanceSaveRequestDto): Result<Unit>

    // --- FUNCȚII NOI ADĂUGATE ---
    suspend fun deactivateVehicle(vehicleId: Int): Result<Unit>
    suspend fun addCustomReminder(vehicleId: Int, request: CustomReminderRequestDto): Result<Unit>
    suspend fun getAllReminderTypes(): Result<List<ReminderTypeResponseDto>>
    suspend fun deactivateCustomReminder(configId: Int): Result<Unit>
    suspend fun resetReminderToDefault(configId: Int): Result<Unit>
}