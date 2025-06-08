package com.example.cartrack.core.data.repository

import android.util.Log
import com.example.cartrack.core.data.api.VehicleApi
import com.example.cartrack.core.data.model.history.MaintenanceLogResponseDto
import com.example.cartrack.core.data.model.maintenance.MaintenanceSaveRequestDto
import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto
import com.example.cartrack.core.data.model.maintenance.ReminderUpdateRequestDto
import com.example.cartrack.core.data.model.vehicle.*
import com.example.cartrack.core.domain.repository.VehicleRepository
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.serialization.SerializationException
import java.io.IOException
import javax.inject.Inject

class VehicleRepositoryImpl @Inject constructor(
    private val api: VehicleApi
) : VehicleRepository {

    private val logTag = "VehicleRepo"

    // --- Generic safeApiCall for GET requests ---
    private suspend fun <T> safeApiCall(
        endpointName: String,
        apiCall: suspend () -> T
    ): Result<T> {
        return try {
            val result = apiCall()
            Log.d(logTag, "Successfully fetched $endpointName.")
            Result.success(result)
        } catch (e: ClientRequestException) {
            val errorMsg = "Client error fetching $endpointName: ${e.response.status.value}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Could not load $endpointName (Error: ${e.response.status.value})."))
        } catch (e: ServerResponseException) {
            val errorMsg = "Server error fetching $endpointName: ${e.response.status.value}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Server error while loading $endpointName."))
        } catch (e: IOException) {
            val errorMsg = "Network error fetching $endpointName: ${e.message}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Network error. Please check your connection."))
        } catch (e: SerializationException) {
            val errorMsg = "Serialization error fetching $endpointName: ${e.message}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Error parsing server response for $endpointName."))
        } catch (e: Exception) {
            val errorMsg = "Unexpected error fetching $endpointName: ${e.message}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("An unexpected error occurred."))
        }
    }

    // --- Generic safeActionCall for POST/PUT/DELETE requests ---
    private suspend fun safeActionCall(
        actionName: String,
        actionCall: suspend () -> HttpResponse
    ): Result<Unit> {
        return try {
            val response = actionCall()
            if (response.status.isSuccess()) {
                Log.d(logTag, "$actionName successful.")
                Result.success(Unit)
            } else {
                val errorBody = runCatching { response.body<String>() }.getOrNull() ?: ""
                val errorMsg = "$actionName failed: ${response.status.description}. $errorBody"
                Log.e(logTag, errorMsg)
                Result.failure(Exception("$actionName failed: ${response.status.description}."))
            }
        } catch (e: Exception) {
            Log.e(logTag, "Exception during $actionName: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- Implementing Repository Methods ---

    override suspend fun getVehiclesByClientId(): Result<List<VehicleResponseDto>> { // Am eliminat clientId
        return safeApiCall("All vehicles for user") {
            api.getVehiclesByClientId().vehicles 
        }
    }

    override suspend fun saveVehicle(request: VehicleSaveRequestDto): Result<Unit> {
        return safeActionCall("Save Vehicle") {
            api.saveVehicle(request)
        }
    }

    override suspend fun getVehicleEngine(vehicleId: Int): Result<VehicleEngineResponseDto> {
        return safeApiCall("Vehicle Engine for ID $vehicleId") {
            api.getVehicleEngine(vehicleId)
        }
    }

    override suspend fun getVehicleModel(vehicleId: Int): Result<VehicleModelResponseDto> {
        return safeApiCall("Vehicle Model for ID $vehicleId") {
            api.getVehicleModel(vehicleId)
        }
    }

    override suspend fun getVehicleInfo(vehicleId: Int): Result<VehicleInfoResponseDto> {
        return safeApiCall("Vehicle Info for ID $vehicleId") {
            api.getVehicleInfo(vehicleId)
        }
    }

    override suspend fun getVehicleBody(vehicleId: Int): Result<VehicleBodyResponseDto> {
        return safeApiCall("Vehicle Body for ID $vehicleId") {
            api.getVehicleBody(vehicleId)
        }
    }

    override suspend fun addMileageReading(vehicleId: Int, mileage: Double): Result<Unit> {
        return safeActionCall("Add Mileage Reading") {
            api.addMileageReading(vehicleId, mileage)
        }
    }

    override suspend fun getRemindersByVehicleId(vehicleId: Int): Result<List<ReminderResponseDto>> {
        return safeApiCall("Reminders for vehicle ID $vehicleId") {
            api.getRemindersByVehicleId(vehicleId)
        }
    }

    override suspend fun getDailyUsage(vehicleId: Int, timeZoneId: String): Result<List<DailyUsageDto>> {
        return safeApiCall("Daily Usage for $vehicleId") {
            api.getDailyUsage(vehicleId, timeZoneId)
        }
    }

    override suspend fun getReminderById(reminderId: Int): Result<ReminderResponseDto> {
        return safeApiCall("Reminder by ID $reminderId") {
            api.getReminderById(reminderId)
        }
    }

    override suspend fun updateReminder(request: ReminderUpdateRequestDto): Result<Unit> {
        return safeActionCall("Update Reminder") {
            api.updateReminder(request)
        }
    }

    override suspend fun updateReminderToDefault(reminderId: Int): Result<Unit> {
        return safeActionCall("Restore Reminder to Default") {
            api.updateReminderToDefault(reminderId)
        }
    }

    override suspend fun updateReminderActiveStatus(reminderId: Int): Result<Unit> {
        return safeActionCall("Update Reminder Active Status") {
            api.updateReminderActiveStatus(reminderId)
        }
    }

    override suspend fun saveVehicleMaintenance(request: MaintenanceSaveRequestDto): Result<Unit> {
        return safeActionCall("Save Vehicle Maintenance") {
            api.addVehicleMaintenance(request)
        }
    }

    override suspend fun getMaintenanceHistory(vehicleId: Int): Result<List<MaintenanceLogResponseDto>> {
        return safeApiCall("Maintenance History for vehicle ID $vehicleId") {
            api.getMaintenanceHistory(vehicleId)
        }
    }
}