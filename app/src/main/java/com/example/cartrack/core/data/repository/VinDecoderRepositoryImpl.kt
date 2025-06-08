package com.example.cartrack.core.data.repository

import com.example.cartrack.core.data.api.VinDecoderApi
import com.example.cartrack.core.data.model.vin.VinDecodedResponseDto
import com.example.cartrack.core.domain.repository.VinDecoderRepository
import javax.inject.Inject

class VinDecoderRepositoryImpl @Inject constructor(
    private val api: VinDecoderApi
) : VinDecoderRepository {
    override suspend fun decodeVin(vin: String): Result<List<VinDecodedResponseDto>> { // Am eliminat clientId
        return try {
            Result.success(api.decodeVin(vin)) // Apel fără clientId
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}