package com.example.cartrack.core.domain.repository

import com.example.cartrack.core.data.model.history.MaintenanceLogResponseDto
import com.example.cartrack.core.data.model.maintenance.MaintenanceSaveRequestDto
import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto
import com.example.cartrack.core.data.model.maintenance.ReminderUpdateRequestDto
import com.example.cartrack.core.data.model.vehicle.*

// Notă: Ideal, aici am folosi modele de domeniu (ex: `Vehicle` în loc de `VehicleResponseDto`).
// Pentru simplitate în acest pas, vom folosi direct DTO-urile.
// Putem introduce modelele de domeniu și mapping-ul mai târziu, ca o altă îmbunătățire.

interface VehicleRepository {
    suspend fun getVehiclesByClientId(): Result<List<VehicleResponseDto>> // Am eliminat clientId
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
    suspend fun updateReminderToDefault(reminderId: Int): Result<Unit>
    suspend fun updateReminderActiveStatus(reminderId: Int): Result<Unit>

    suspend fun getMaintenanceHistory(vehicleId: Int): Result<List<MaintenanceLogResponseDto>>
    suspend fun saveVehicleMaintenance(request: MaintenanceSaveRequestDto): Result<Unit>
}