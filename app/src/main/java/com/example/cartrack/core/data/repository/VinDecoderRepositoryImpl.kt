package com.example.cartrack.core.data.repository

import com.example.cartrack.core.data.api.VinDecoderApi
import com.example.cartrack.core.data.model.vin.VinDecodedResponseDto
import com.example.cartrack.core.domain.repository.VinDecoderRepository
import javax.inject.Inject

class VinDecoderRepositoryImpl @Inject constructor(
    private val api: VinDecoderApi
) : VinDecoderRepository {
    // Adăugăm clientId și îl pasăm mai departe către API
    override suspend fun decodeVin(vin: String, clientId: Int): Result<List<VinDecodedResponseDto>> {
        return try {
            Result.success(api.decodeVin(vin, clientId))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to decode VIN: ${e.message}", e))
        }
    }
}