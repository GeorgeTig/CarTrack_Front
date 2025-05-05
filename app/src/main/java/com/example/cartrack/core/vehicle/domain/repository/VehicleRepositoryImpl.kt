package com.example.cartrack.core.vehicle.domain.repository

import android.util.Log
import com.example.cartrack.core.vehicle.data.api.VehicleApi
import com.example.cartrack.core.vehicle.data.model.VehicleResponseDto
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import kotlinx.serialization.SerializationException
import java.io.IOException
import javax.inject.Inject

class VehicleRepositoryImpl @Inject constructor(
    private val apiService: VehicleApi
) : VehicleRepository {

    private val logTag = "VehicleRepo"

    override suspend fun getVehiclesByClientId(clientId: Int): Result<List<VehicleResponseDto>> {
        return try {

            val apiResponse = apiService.getVehiclesByClientId(clientId)

            val vehicles = apiResponse.result

            Log.d(logTag, "Successfully fetched ${vehicles.size} vehicles for client $clientId")
            Result.success(vehicles)

        } catch (e: ClientRequestException) { // 4xx errors
            val errorBody = runCatching { e.response.body<String>() }.getOrNull()
            val errorMsg = "Error fetching vehicles for client $clientId: ${e.response.status.description} (${e.response.status.value}). ${errorBody ?: ""}"
            Log.e(logTag, errorMsg, e)
            if (e.response.status.value == 401 || e.response.status.value == 403) {
                Result.failure(Exception("Authentication error fetching vehicles. Please login again."))
            } else {
                Result.failure(Exception("Client error fetching vehicles: ${e.response.status.description}"))
            }
        } catch (e: ServerResponseException) { // 5xx errors
            val errorMsg = "Error fetching vehicles for client $clientId: Server error ${e.response.status.value}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Server error fetching vehicles. Please try again later."))
        } catch (e: IOException) { // Network errors
            val errorMsg = "Error fetching vehicles for client $clientId: Network error: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Network error. Please check your connection."))
        } catch (e: SerializationException) { // JSON parsing errors
            val errorMsg = "Error fetching vehicles for client $clientId: Error parsing response: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("An unexpected error occurred parsing server response."))
        } catch (e: Exception) { // Catch-all for other unexpected errors
            val errorMsg = "Error fetching vehicles for client $clientId: Unexpected error: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("An unexpected error occurred fetching vehicles."))
        }
    }
}
