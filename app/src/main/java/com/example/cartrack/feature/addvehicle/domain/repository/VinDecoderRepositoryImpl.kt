package com.example.cartrack.feature.addvehicle.domain.repository

import android.util.Log
import com.example.cartrack.feature.addvehicle.data.api.VinDecoderApi // Import API from this feature
import com.example.cartrack.feature.addvehicle.data.model.VinDecodedResponseDto // Import model from this feature
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import kotlinx.serialization.SerializationException
import java.io.IOException
import javax.inject.Inject

class VinDecoderRepositoryImpl @Inject constructor(
    private val apiService: VinDecoderApi // Inject API from this feature
): VinDecoderRepository {

    private val logTag = "VinDecoderRepo" // Tag for logging

    override suspend fun decodeVin(vin: String, clientId: Int): Result<List<VinDecodedResponseDto>> {
        return try {
            Log.d(logTag, "Decoding VIN: $vin for client: $clientId")
            val decodedInfo = apiService.decodeVin(vin, clientId)
            Log.d(logTag, "Successfully decoded VIN. Result count: ${decodedInfo.size}")
            Result.success(decodedInfo) // Return the list wrapped in Result.success
        } catch (e: ClientRequestException) { // Handle 4xx errors
            val errorBody = runCatching { e.response.body<String>() }.getOrNull()
            val errorMsg = "Error decoding VIN $vin: ${e.response.status.description} (${e.response.status.value}). ${errorBody ?: ""}"
            Log.e(logTag, errorMsg, e)
            // Provide specific user messages based on status code from backend logic
            when (e.response.status.value) {
                400 -> Result.failure(Exception("Invalid VIN format or Client ID."))
                401, 403 -> Result.failure(Exception("Authentication error decoding VIN."))
                else -> Result.failure(Exception("Client error decoding VIN: ${e.response.status.description}"))
            }
        } catch (e: ServerResponseException) { // Handle 5xx errors
            val errorMsg = "Error decoding VIN $vin: Server error ${e.response.status.value}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Server error decoding VIN. Please try again later."))
        } catch (e: IOException) { // Handle network errors
            val errorMsg = "Error decoding VIN $vin: Network error: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Network error decoding VIN. Please check connection."))
        } catch (e: SerializationException) { // Handle JSON parsing errors
            val errorMsg = "Error decoding VIN $vin: Error parsing response: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Error parsing VIN details from server."))
        } catch (e: Exception) { // Handle any other unexpected errors
            val errorMsg = "Error decoding VIN $vin: Unexpected error: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("An unexpected error occurred while decoding VIN."))
        }
    }
}