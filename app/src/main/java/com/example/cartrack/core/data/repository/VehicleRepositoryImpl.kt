package com.example.cartrack.core.data.repository

import com.example.cartrack.core.data.api.VehicleApi
import com.example.cartrack.core.data.api.safeApiCall
import com.example.cartrack.core.data.model.history.MaintenanceLogResponseDto
import com.example.cartrack.core.data.model.maintenance.MaintenanceSaveRequestDto
import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto
import com.example.cartrack.core.data.model.maintenance.ReminderUpdateRequestDto
import com.example.cartrack.core.data.model.vehicle.*
import com.example.cartrack.core.domain.repository.AuthRepository
import com.example.cartrack.core.domain.repository.VehicleRepository
import javax.inject.Inject
import javax.inject.Provider

class VehicleRepositoryImpl @Inject constructor(
    private val api: VehicleApi,
    private val authRepositoryProvider: Provider<AuthRepository>
) : VehicleRepository {

    override suspend fun getVehiclesByClientId(): Result<List<VehicleResponseDto>> =
        safeApiCall(authRepositoryProvider, "All vehicles") { api.getVehiclesByClientId().vehicles }

    override suspend fun saveVehicle(request: VehicleSaveRequestDto): Result<Unit> =
        safeApiCall(authRepositoryProvider, "Save Vehicle") { api.saveVehicle(request); Unit }

    override suspend fun getVehicleEngine(vehicleId: Int): Result<VehicleEngineResponseDto> =
        safeApiCall(authRepositoryProvider, "Vehicle Engine") { api.getVehicleEngine(vehicleId) }

    override suspend fun getVehicleModel(vehicleId: Int): Result<VehicleModelResponseDto> =
        safeApiCall(authRepositoryProvider, "Vehicle Model") { api.getVehicleModel(vehicleId) }

    override suspend fun getVehicleInfo(vehicleId: Int): Result<VehicleInfoResponseDto> =
        safeApiCall(authRepositoryProvider, "Vehicle Info") { api.getVehicleInfo(vehicleId) }

    override suspend fun getVehicleBody(vehicleId: Int): Result<VehicleBodyResponseDto> =
        safeApiCall(authRepositoryProvider, "Vehicle Body") { api.getVehicleBody(vehicleId) }

    override suspend fun addMileageReading(vehicleId: Int, mileage: Double): Result<Unit> =
        safeApiCall(authRepositoryProvider, "Add Mileage") { api.addMileageReading(vehicleId, mileage); Unit }

    override suspend fun getRemindersByVehicleId(vehicleId: Int): Result<List<ReminderResponseDto>> =
        safeApiCall(authRepositoryProvider, "Reminders") { api.getRemindersByVehicleId(vehicleId) }

    override suspend fun getDailyUsage(vehicleId: Int, timeZoneId: String): Result<List<DailyUsageDto>> =
        safeApiCall(authRepositoryProvider, "Daily Usage") { api.getDailyUsage(vehicleId, timeZoneId) }

    override suspend fun getReminderById(reminderId: Int): Result<ReminderResponseDto> =
        safeApiCall(authRepositoryProvider, "Reminder by ID") { api.getReminderById(reminderId) }

    override suspend fun updateReminder(request: ReminderUpdateRequestDto): Result<Unit> =
        safeApiCall(authRepositoryProvider, "Update Reminder") { api.updateReminder(request); Unit }

    override suspend fun updateReminderToDefault(reminderId: Int): Result<Unit> =
        safeApiCall(authRepositoryProvider, "Restore Reminder") { api.updateReminderToDefault(reminderId); Unit }

    override suspend fun updateReminderActiveStatus(reminderId: Int): Result<Unit> =
        safeApiCall(authRepositoryProvider, "Update Reminder Status") { api.updateReminderActiveStatus(reminderId); Unit }

    override suspend fun saveVehicleMaintenance(request: MaintenanceSaveRequestDto): Result<Unit> =
        safeApiCall(authRepositoryProvider, "Save Maintenance") { api.addVehicleMaintenance(request); Unit }

    override suspend fun getMaintenanceHistory(vehicleId: Int): Result<List<MaintenanceLogResponseDto>> =
        safeApiCall(authRepositoryProvider, "Maintenance History") { api.getMaintenanceHistory(vehicleId) }
}