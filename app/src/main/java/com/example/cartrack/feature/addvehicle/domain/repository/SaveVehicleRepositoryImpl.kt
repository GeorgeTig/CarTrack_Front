package com.example.cartrack.feature.addvehicle.domain.repository

import android.util.Log
import com.example.cartrack.feature.addvehicle.data.api.VehicleApi
import com.example.cartrack.feature.addvehicle.data.model.VehicleSaveRequestDto
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import kotlinx.serialization.SerializationException
import java.io.IOException
import javax.inject.Inject

class SaveVehicleRepositoryImpl @Inject constructor(
    private val vehicleApi: VehicleApi
) : SaveVehicleRepository {

    private val logTag = "SaveVehicleRepo"

    override suspend fun saveVehicle(request: VehicleSaveRequestDto): Result<Unit> {
        return try {
            Log.d(logTag, "Attempting to save vehicle: VIN=${request.vin}, ModelID=${request.modelId}")
            vehicleApi.saveVehicle(request)
            Log.d(logTag, "Vehicle saved successfully via API.")
            Result.success(Unit) // Indicate success

        } catch (e: ClientRequestException) { // Handle 4xx errors
            val errorBody = runCatching { e.response.body<String>() }.getOrNull()
            val errorMsg = "Error saving vehicle: ${e.response.status.description} (${e.response.status.value}). ${errorBody ?: ""}"
            Log.e(logTag, errorMsg, e)
            when (e.response.status.value) {
                400 -> Result.failure(Exception("Invalid vehicle data submitted."))
                401, 403 -> Result.failure(Exception("Authentication error saving vehicle."))
                // Add other specific 4xx handlers if needed
                else -> Result.failure(Exception("Client error saving vehicle: ${e.response.status.description}"))
            }
        } catch (e: ServerResponseException) { // Handle 5xx errors
            val errorMsg = "Error saving vehicle: Server error ${e.response.status.value}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Server error saving vehicle. Please try again later."))
        } catch (e: IOException) { // Handle network errors
            val errorMsg = "Error saving vehicle: Network error: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Network error saving vehicle. Please check connection."))
        } catch (e: SerializationException) { // Handle JSON request serialization errors (less likely for request)
            val errorMsg = "Error saving vehicle: Error preparing data: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Internal error preparing vehicle data."))
        } catch (e: Exception) { // Handle any other unexpected errors
            val errorMsg = "Error saving vehicle: Unexpected error: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("An unexpected error occurred while saving the vehicle."))
        }
    }
}