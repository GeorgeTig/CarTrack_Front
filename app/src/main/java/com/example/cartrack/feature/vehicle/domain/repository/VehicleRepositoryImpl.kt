package com.example.cartrack.feature.vehicle.domain.repository


import android.util.Log
import com.example.cartrack.feature.vehicle.data.api.VehicleApi
import com.example.cartrack.feature.vehicle.data.model.VehicleResponseDto
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import kotlinx.serialization.SerializationException
import java.io.IOException
import javax.inject.Inject

class VehicleRepositoryImpl @Inject constructor(
    private val apiService: VehicleApi
) : VehicleRepository {

    private val logTag = "VehicleRepo" // Tag for logging

    override suspend fun getVehiclesByClientId(clientId: Int): Result<List<VehicleResponseDto>> {
        return try {
            // *** CHANGE: Call API which returns the wrapper object ***
            val apiResponse = apiService.getVehiclesByClientId(clientId)

            // *** CHANGE: Extract the list from the wrapper object's 'result' field ***
            val vehicles = apiResponse.result

            Log.d(logTag, "Successfully fetched ${vehicles.size} vehicles for client $clientId")
            Result.success(vehicles) // Return the extracted list

        } catch (e: ClientRequestException) {
            val errorBody = runCatching { e.response.body<String>() }.getOrNull()
            val errorMsg = "Error fetching vehicles for client $clientId: ${e.response.status.description} (${e.response.status.value}). ${errorBody ?: ""}"
            Log.e(logTag, errorMsg, e)
            if (e.response.status.value == 401 || e.response.status.value == 403) {
                Result.failure(Exception("Authentication error fetching vehicles. Please login again."))
            } else {
                Result.failure(Exception("Client error fetching vehicles: ${e.response.status.description}"))
            }
        } catch (e: ServerResponseException) {
            val errorMsg = "Error fetching vehicles for client $clientId: Server error ${e.response.status.value}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Server error fetching vehicles. Please try again later."))
        } catch (e: IOException) {
            val errorMsg = "Error fetching vehicles for client $clientId: Network error: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Network error. Please check your connection."))
        } catch (e: SerializationException) {
            // This error can now happen if the VehicleListApiResponse structure doesn't match
            // OR if the nested VehicleResponseDto structure doesn't match.
            val errorMsg = "Error fetching vehicles for client $clientId: Error parsing response: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("An unexpected error occurred parsing server response."))
        } catch (e: Exception) {
            val errorMsg = "Error fetching vehicles for client $clientId: Unexpected error: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("An unexpected error occurred fetching vehicles."))
        }
    }
}
