package com.example.cartrack.core.vehicle.domain.repository

import android.util.Log
import com.example.cartrack.core.vehicle.data.api.VehicleApi
import com.example.cartrack.core.vehicle.data.model.ReminderRequestDto
import com.example.cartrack.core.vehicle.data.model.ReminderResponseDto
import com.example.cartrack.core.vehicle.data.model.VehicleBodyResponseDto
import com.example.cartrack.core.vehicle.data.model.VehicleEngineResponseDto
import com.example.cartrack.core.vehicle.data.model.VehicleInfoResponseDto
import com.example.cartrack.core.vehicle.data.model.VehicleListResponse
import com.example.cartrack.core.vehicle.data.model.VehicleModelResponseDto
import com.example.cartrack.core.vehicle.data.model.VehicleResponseDto
import com.example.cartrack.core.vehicle.data.model.VehicleUsageStatsResponseDto
import com.example.cartrack.feature.addmaintenance.data.MaintenanceSaveRequestDto
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.serialization.SerializationException
import java.io.IOException
import javax.inject.Inject

class VehicleRepositoryImpl @Inject constructor(
    private val apiService: VehicleApi
) : VehicleRepository {

    private val logTag = "VehicleRepo"

    override suspend fun getVehiclesByClientId(clientId: Int): Result<List<VehicleResponseDto>> {
        return try {
           val apiResponse: VehicleListResponse = apiService.getVehiclesByClientId(clientId)
            val vehicles = apiResponse.result

            Log.d(logTag, "Successfully fetched ${vehicles.size} vehicles for client $clientId")
            Result.success(vehicles)
        } catch (e: ClientRequestException) {
            val errorBody = runCatching { e.response.body<String>() }.getOrNull()
            val errorMsg = "Error fetching vehicle list for client $clientId: ${e.response.status.description} (${e.response.status.value}). ${errorBody ?: ""}"
            Log.e(logTag, errorMsg, e)
            if (e.response.status.value == 401 || e.response.status.value == 403) {
                Result.failure(Exception("Authentication error fetching vehicles."))
            } else {
                Result.failure(Exception("Client error fetching vehicles: ${e.response.status.description}"))
            }
        } catch (e: ServerResponseException) {
            val errorMsg = "Error fetching vehicle list for client $clientId: Server error ${e.response.status.value}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Server error fetching vehicles."))
        } catch (e: IOException) {
            val errorMsg = "Error fetching vehicle list for client $clientId: Network error: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Network error. Please check connection."))
        } catch (e: SerializationException) {
            val errorMsg = "Error fetching vehicle list for client $clientId: Error parsing response: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Error parsing server response for vehicle list."))
        } catch (e: Exception) {
            val errorMsg = "Error fetching vehicle list for client $clientId: Unexpected error: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Unexpected error fetching vehicles."))
        }
    }

    // --- Generic safeApiCall for GET requests that return a specific DTO body ---
    // Changed 'vehicleId: Int' to 'identifier: Any' for more generic logging.
    private suspend fun <T> safeApiCallForDetails( // Renamed for clarity from the action one
        apiCall: suspend () -> T,
        endpointName: String,
        identifier: Any
    ): Result<T> {
        return try {
            val result = apiCall()
            Log.d(logTag, "Successfully fetched $endpointName for ID $identifier")
            Result.success(result)
        } catch (e: ClientRequestException) {
            val errorBody = runCatching { e.response.body<String>() }.getOrNull()
            val errorMsg = "Error fetching $endpointName for ID $identifier: ${e.response.status.description} (${e.response.status.value}). ${errorBody ?: ""}"
            Log.e(logTag, errorMsg, e)
            if (e.response.status.value == 401 || e.response.status.value == 403) {
                Result.failure(Exception("Authentication error fetching $endpointName."))
            } else if (e.response.status.value == 404) {
                Result.failure(Exception("$endpointName not found for ID $identifier."))
            } else {
                Result.failure(Exception("Client error fetching $endpointName: ${e.response.status.description}"))
            }
        } catch (e: ServerResponseException) {
            val errorMsg = "Error fetching $endpointName for ID $identifier: Server error ${e.response.status.value}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Server error fetching $endpointName."))
        } catch (e: IOException) {
            val errorMsg = "Error fetching $endpointName for ID $identifier: Network error: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Network error fetching $endpointName."))
        } catch (e: SerializationException) {
            val errorMsg = "Error fetching $endpointName for ID $identifier: Error parsing response: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Error parsing server response for $endpointName."))
        } catch (e: Exception) {
            val errorMsg = "Error fetching $endpointName for ID $identifier: Unexpected error: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Unexpected error fetching $endpointName."))
        }
    }

    // --- Detail GET methods using the corrected safeApiCallForDetails ---
    override suspend fun getVehicleEngine(vehicleId: Int): Result<VehicleEngineResponseDto> {
        return safeApiCallForDetails(
            apiCall = { apiService.getVehicleEngine(vehicleId) },
            endpointName = "Vehicle Engine",
            identifier = vehicleId
        )
    }

    override suspend fun getVehicleModel(vehicleId: Int): Result<VehicleModelResponseDto> {
        return safeApiCallForDetails(
            apiCall = { apiService.getVehicleModel(vehicleId) },
            endpointName = "Vehicle Model",
            identifier = vehicleId
        )
    }

    override suspend fun getVehicleInfo(vehicleId: Int): Result<VehicleInfoResponseDto> {
        return safeApiCallForDetails(
            apiCall = { apiService.getVehicleInfo(vehicleId) },
            endpointName = "Vehicle Info",
            identifier = vehicleId
        )
    }

    override suspend fun getVehicleUsageStats(vehicleId: Int): Result<VehicleUsageStatsResponseDto> {
        return safeApiCallForDetails(
            apiCall = { apiService.getVehicleUsageStats(vehicleId) },
            endpointName = "Vehicle Usage Stats",
            identifier = vehicleId
        )
    }

    override suspend fun getVehicleBody(vehicleId: Int): Result<VehicleBodyResponseDto> {
        return safeApiCallForDetails(
            apiCall = { apiService.getVehicleBody(vehicleId) },
            endpointName = "Vehicle Body",
            identifier = vehicleId
        )
    }

    override suspend fun getRemindersByVehicleId(vehicleId: Int): Result<List<ReminderResponseDto>> {
        return safeApiCallForDetails( // Can reuse the same helper if API returns body directly
            apiCall = { apiService.getRemindersByVehicleId(vehicleId) },
            endpointName = "Vehicle Reminders",
            identifier = vehicleId
        )
    }

    // --- Generic safeActionCall for POST/PUT/DELETE requests (no body expected in Result.success) ---
    private suspend fun safeActionCall(
        actionCall: suspend () -> HttpResponse,
        actionName: String,
        identifier: Any? = null
    ): Result<Unit> {
        return try {
            val response = actionCall()
            if (response.status.isSuccess()) {
                Log.d(logTag, "$actionName successful" + (identifier?.let { " for ID $it" } ?: ""))
                Result.success(Unit)
            } else {
                val errorBody = runCatching { response.body<String>() }.getOrNull() ?: ""
                val errorMsg = "$actionName failed: ${response.status.description} (${response.status.value})" +
                        (identifier?.let { " for ID $it" } ?: "") + ". $errorBody"
                Log.e(logTag, errorMsg)
                // Provide more specific error message if possible
                Result.failure(Exception("$actionName failed: ${response.status.description}. $errorBody".trim()))
            }
        } catch (e: ClientRequestException) {
            val errorBody = runCatching { e.response.body<String>() }.getOrNull() ?: ""
            val errorMsg = "$actionName error: ${e.response.status.description} (${e.response.status.value})" +
                    (identifier?.let { " for ID $it" } ?: "") + ". $errorBody"
            Log.e(logTag, errorMsg, e)
            if (e.response.status.value == 401 || e.response.status.value == 403) {
                Result.failure(Exception("Authentication error during $actionName."))
            } else {
                Result.failure(Exception("Client error during $actionName: ${e.response.status.description}. $errorBody".trim()))
            }
        } catch (e: ServerResponseException) {
            val errorMsg = "$actionName server error: ${e.response.status.value}" + (identifier?.let { " for ID $it" } ?: "")
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Server error during $actionName."))
        } catch (e: IOException) {
            val errorMsg = "$actionName network error: ${e.localizedMessage}" + (identifier?.let { " for ID $it" } ?: "")
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Network error during $actionName."))
        } catch (e: Exception) { // Catch-all including SerializationException if body parsing fails
            val errorMsg = "$actionName unexpected error: ${e.localizedMessage}" + (identifier?.let { " for ID $it" } ?: "")
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Unexpected error during $actionName."))
        }
    }

    // --- Update/Action methods using safeActionCall ---
    override suspend fun updateReminder(request: ReminderRequestDto): Result<Unit> {
        return safeActionCall(
            actionCall = { apiService.updateReminder(request) },
            actionName = "Update Reminder",
            identifier = request.id
        )
    }

    override suspend fun updateReminderToDefault(reminderId: Int): Result<Unit> {
        return safeActionCall(
            actionCall = { apiService.updateReminderToDefault(reminderId) },
            actionName = "Restore Reminder to Default",
            identifier = reminderId
        )
    }

    override suspend fun updateReminderActiveStatus(reminderId: Int): Result<Unit> {
        return safeActionCall(
            actionCall = { apiService.updateReminderActiveStatus(reminderId) },
            actionName = "Update Reminder Active Status",
            identifier = reminderId
        )
    }

    override suspend fun saveVehicleMaintenance(request: MaintenanceSaveRequestDto): Result<Unit> {
        return safeActionCall(
            actionCall = { apiService.addVehicleMaintenance(request) }, // Apelul metodei din VehicleApi
            actionName = "Save Vehicle Maintenance",
            identifier = request.vehicleId
        )
    }
}